package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.*;
import io.vertx.core.file.FileSystem;
import com.github.javaparser.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import pcd.ass02.dependencyAnalyserLib.report.ClassDepsReport;
import pcd.ass02.dependencyAnalyserLib.report.PackageDepsReport;
import pcd.ass02.dependencyAnalyserLib.report.ProjectDepsReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DependencyAnalyserLib {

    public static final String JAVA_EXTENSION = ".java";

    public DependencyAnalyserLib() { }

    /**
     * Get the dependencies of a class.
     * @param classSrcFile the class file to analyze
     * @param vertx the Vertx instance
     * @return the class dependencies list
     */
    public static Future<ClassDepsReport> getClassDependencies(String classSrcFile, Vertx vertx) {
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
            Set<String> variableNames = cu.findAll(VariableDeclarator.class).stream()
                    .map(VariableDeclarator::getNameAsString)
                    .collect(Collectors.toSet());
            List<String> dependencies = cu.findAll(ClassOrInterfaceType.class).stream()
                    .map(ClassOrInterfaceType::getNameAsString)
                    .filter(name -> !variableNames.contains(name))
                    .distinct()
                    .toList();
            return new ClassDepsReport(classSrcFile, dependencies);
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
