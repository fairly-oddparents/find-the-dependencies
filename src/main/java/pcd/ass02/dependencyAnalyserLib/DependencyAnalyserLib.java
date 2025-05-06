package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.*;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import io.vertx.core.file.FileSystem;
import com.github.javaparser.*;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import pcd.ass02.dependencyAnalyserLib.report.ClassDepsReport;
import com.github.javaparser.ast.CompilationUnit;
import pcd.ass02.dependencyAnalyserLib.report.PackageDepsReport;
import pcd.ass02.dependencyAnalyserLib.report.ProjectDepsReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class DependencyAnalyserLib {

    public static final String JAVA_EXTENSION = ".java";

    public DependencyAnalyserLib() { }

    /**
     * Get the dependencies of a class.
     *
     * @param classSrcFile the class file to analyze
     * @param vertx        the Vertx instance
     * @return the class dependencies list
     */
    public static Future<ClassDepsReport> getClassDependencies(String classSrcFile, Vertx vertx) {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver(false));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        config.setSymbolResolver(symbolSolver);
        StaticJavaParser.setConfiguration(config);

        FileSystem fileSystem = vertx.fileSystem();
        if (!classSrcFile.endsWith(JAVA_EXTENSION)) {
            throw new IllegalArgumentException("File is not a Java file");
        }
        return fileSystem.exists(classSrcFile).compose(exists ->
                exists ? fileSystem.lprops(classSrcFile)
                : Future.failedFuture(new IllegalArgumentException("File not found"))
        ).compose(fileProps ->
                fileProps.isDirectory() ? Future.failedFuture(new IllegalArgumentException("Path is a directory"))
                : fileSystem.readFile(classSrcFile)
        ).map(file -> {
            CompilationUnit cu = StaticJavaParser.parse(file.toString());
            List<String> dependencies = new ArrayList<>();

            // Tipi usati direttamente
            cu.findAll(ClassOrInterfaceType.class).forEach(type -> {
                try {
                    ResolvedType resolved = type.resolve();
                    if (resolved.isReferenceType()) {
                        String qualifiedName = resolved.asReferenceType().getQualifiedName();
                        dependencies.add(qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1));
                    }
                } catch (RuntimeException ignored) {}
            });

            // Chiamate a metodi (anche statici)
            cu.findAll(MethodCallExpr.class).forEach(expr -> {
                try {
                    ResolvedMethodDeclaration method = expr.resolve();
                    dependencies.add(method.declaringType().getClassName());
                } catch (RuntimeException ignored) {}
            });

            // Accessi a campi (statici o meno)
            cu.findAll(FieldAccessExpr.class).forEach(expr -> {
                try {
                    ResolvedValueDeclaration resolved = expr.resolve();
                    if (resolved instanceof ResolvedFieldDeclaration field) {
                        dependencies.add(field.declaringType().getClassName());
                    } else if (resolved instanceof ResolvedEnumConstantDeclaration enumConst) {
                        String qualifiedName = enumConst.getType().asReferenceType().getQualifiedName();
                        dependencies.add(qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1));
                    }
                } catch (RuntimeException ignored) {}
            });

            // Istanziazione oggetti con new
            cu.findAll(ObjectCreationExpr.class).forEach(expr -> {
                try {
                    ResolvedConstructorDeclaration constructor = expr.resolve();
                    dependencies.add(constructor.getClassName());
                } catch (RuntimeException ignored) {}
            });

            return new ClassDepsReport(classSrcFile, dependencies.stream().distinct().toList());
        });
    }

    /**
     * Get the dependencies of a package.
     * @param packageSrcFolder the path of the package to analyze
     * @param vertx the Vertx instance
     * @return the package dependencies list
     */
    public static Future<PackageDepsReport> getPackageDependencies(String packageSrcFolder, Vertx vertx) {
        FileSystem fileSystem = vertx.fileSystem();
        return fileSystem.exists(packageSrcFolder).compose(exists ->
                exists ? fileSystem.lprops(packageSrcFolder)
                : Future.failedFuture(new IllegalArgumentException("Path not found"))
        ).compose(dirProps ->
                !dirProps.isDirectory() ? Future.failedFuture(new IllegalArgumentException("Path is not a directory"))
                : fileSystem.readDir(packageSrcFolder)
        ).compose(files -> Future.all(files.stream()
                .filter(file -> file.endsWith(JAVA_EXTENSION))
                .map(file -> getClassDependencies(file, vertx))
                .toList()
        )).compose(composite -> Future.succeededFuture(new PackageDepsReport(
                packageSrcFolder,
                composite.result().list()
        )));
    }

    /**
     * Get the dependencies of a project.
     * @param projectSrcFolder the path of the project to analyze
     * @param vertx the Vertx instance
     * @return the project dependencies list
     */
    public static Future<ProjectDepsReport> getProjectDependencies(String projectSrcFolder, Vertx vertx) {
        FileSystem fileSystem = vertx.fileSystem();
        return fileSystem.exists(projectSrcFolder).compose(exists ->
                exists ? fileSystem.lprops(projectSrcFolder)
                        : Future.failedFuture(new IllegalArgumentException("Path not found"))
        ).compose(dirProps ->
                !dirProps.isDirectory() ? Future.failedFuture(new IllegalArgumentException("Path is not a directory"))
                        : fileSystem.readDir(projectSrcFolder)
        ).compose(entries -> {
            List<Future<PackageDepsReport>> packageFutures = new ArrayList<>();
            List<Future<ProjectDepsReport>> subDirFutures = new ArrayList<>();

            for (String entry : entries) {
                Path p = Paths.get(entry);
                if (!Files.isDirectory(p)) continue;
                try (Stream<Path> files = Files.walk(p, 1)) {
                    boolean hasJava = files.anyMatch(f -> Files.isRegularFile(f)
                            && f.toString().endsWith(JAVA_EXTENSION));
                    if (hasJava) {
                        packageFutures.add(getPackageDependencies(entry, vertx));
                    } else {
                        subDirFutures.add(getProjectDependencies(entry, vertx));
                    }
                } catch (IOException e) {
                    return Future.failedFuture(e);
                }
            }

            Future<List<PackageDepsReport>> packages = Future.all(packageFutures).map(CompositeFuture::list);
            Future<List<PackageDepsReport>> nestedPackages = Future
                    .all(subDirFutures)
                    .map(cf -> cf.list().stream()
                            .flatMap(p -> ((ProjectDepsReport) p).getDependencies().stream())
                            .toList());

            return packages.compose(list -> nestedPackages.map(nested -> {
                List<PackageDepsReport> all = new ArrayList<>(list);
                all.addAll(nested);
                return new ProjectDepsReport(projectSrcFolder, all);
            }));
        });
    }
}
