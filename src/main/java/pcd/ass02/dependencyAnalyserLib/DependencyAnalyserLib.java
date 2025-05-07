package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.*;
import io.vertx.core.file.FileSystem;
import com.github.javaparser.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import pcd.ass02.dependencyAnalyserLib.report.ClassDepsReport;
import pcd.ass02.dependencyAnalyserLib.report.PackageDepsReport;
import pcd.ass02.dependencyAnalyserLib.report.ProjectDepsReport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class DependencyAnalyserLib {

    public static final String FILE_EXTENSION = ".java";
    public static final String SRC_FOLDER = "java";

    public DependencyAnalyserLib() { }

    /**
     * Get the dependencies of a class.
     * @param classSrcFile the class file to analyze
     * @param vertx the Vertx instance
     * @return the class dependencies list
     */
    public static Future<ClassDepsReport> getClassDependencies(String classSrcFile, Vertx vertx) {
        Path sourceRoot = findSourceRoot(Paths.get(classSrcFile));
        if (sourceRoot == null) {
            throw new IllegalArgumentException("Cannot locate 'java' folder in the path");
        }

        CombinedTypeSolver solver = new CombinedTypeSolver(
                new ReflectionTypeSolver(false),
                new JavaParserTypeSolver(sourceRoot.toFile())
        );

        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21)
                .setSymbolResolver(new JavaSymbolSolver(solver));
        JavaParser parser = new JavaParser(config);

        FileSystem fileSystem = vertx.fileSystem();
        if (!classSrcFile.endsWith(FILE_EXTENSION)) {
            throw new IllegalArgumentException("Not a Java file");
        }

        return fileSystem.exists(classSrcFile).compose(exists ->
                exists ? fileSystem.lprops(classSrcFile) : Future.failedFuture("File not found")
        ).compose(props -> props.isDirectory()
                ? Future.failedFuture("Path is a directory")
                : fileSystem.readFile(classSrcFile)
        ).map(file -> {
            CompilationUnit cu = parser.parse(file.toString()).getResult()
                    .orElseThrow(() -> new RuntimeException("Parsing failed"));
            List<String> deps = collectDependencies(cu);
            return new ClassDepsReport(classSrcFile, deps);
        });
    }

    public static Path findSourceRoot(Path path) {
        while (path != null && !path.getFileName().toString().equals(SRC_FOLDER)) {
            path = path.getParent();
        }
        return path;
    }

    public static List<String> collectDependencies(CompilationUnit cu) {
        List<String> deps = new ArrayList<>();

        cu.findAll(ClassOrInterfaceType.class).forEach(t -> tryResolve(() ->
                deps.add(t.resolve().asReferenceType().getQualifiedName())));

        cu.findAll(MethodCallExpr.class).forEach(e -> tryResolve(() ->
                deps.add(e.resolve().declaringType().getQualifiedName())));

        cu.findAll(FieldAccessExpr.class).forEach(e -> tryResolve(() -> {
            ResolvedValueDeclaration r = e.resolve();
            if (r instanceof ResolvedFieldDeclaration f)
                deps.add(f.declaringType().getQualifiedName());
            else if (r instanceof ResolvedEnumConstantDeclaration en)
                deps.add(en.getType().asReferenceType().getQualifiedName());
        }));

        cu.findAll(ObjectCreationExpr.class).forEach(e -> tryResolve(() ->
                deps.add(e.resolve().declaringType().getQualifiedName())));

        return deps.stream().distinct().sorted().toList();
    }

    private static void tryResolve(Runnable r) {
        try { r.run(); } catch (RuntimeException ignored) {}
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
                .filter(file -> file.endsWith(FILE_EXTENSION))
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
        return fileSystem.exists(projectSrcFolder).compose(exists -> exists
                ? fileSystem.lprops(projectSrcFolder)
                : Future.failedFuture("Path not found")
        ).compose(props -> props.isDirectory()
                ? scanForPackages(projectSrcFolder, vertx)
                : Future.failedFuture("Path is not a directory")
        ).compose(packageFolders -> {
            List<Future<PackageDepsReport>> packageDeps = packageFolders.stream()
                    .map(folder -> getPackageDependencies(folder, vertx))
                    .toList();
            return Future.all(packageDeps).map(cf ->
                    new ProjectDepsReport(projectSrcFolder, cf.list())
            );
        });
    }

    private static Future<List<String>> scanForPackages(String dir, Vertx vertx) {
        FileSystem fileSystem = vertx.fileSystem();
        return fileSystem.readDir(dir).compose(entries -> {
            List<Future<List<String>>> subCalls = new ArrayList<>();
            boolean hasJava = entries.stream().anyMatch(f -> f.endsWith(FILE_EXTENSION));

            for (String entry : entries) {
                subCalls.add(fileSystem.lprops(entry).compose(props ->
                        props.isDirectory() ? scanForPackages(entry, vertx)
                                : Future.succeededFuture(List.of())
                ));
            }

            return Future.all(subCalls).map(cf -> {
                List<String> result = new ArrayList<>();
                if (hasJava) result.add(dir);
                for (int i = 0; i < cf.size(); i++) {
                    result.addAll(cf.resultAt(i));
                }
                return result;
            });
        });
    }
}
