package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.vertx.core.file.FileSystem;
import pcd.ass02.dependencyAnalyserLib.report.ClassDepsReport;
import pcd.ass02.dependencyAnalyserLib.report.PackageDepsReport;
import pcd.ass02.dependencyAnalyserLib.report.ProjectDepsReport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public final class DependencyAnalyserLib {

    public DependencyAnalyserLib() { }

    /**
     * Get the dependencies of a class.
     * @param classSrcFile the class file to analyze
     * @param vertx the Vertx instance
     * @return the class dependencies list
     */
    public static Future<ClassDepsReport> getClassDependencies(String classSrcFile, Vertx vertx) {
        FileSystem fileSystem = vertx.fileSystem();
        if (!classSrcFile.endsWith(".java")) {
            throw new IllegalArgumentException("File is not a Java file");
        }
        return fileSystem.exists(classSrcFile).compose(exists ->
                exists ? fileSystem.lprops(classSrcFile)
                : Future.failedFuture(new IllegalArgumentException("File not found"))
        ).compose(fileProps ->
                fileProps.isDirectory() ? Future.failedFuture(new IllegalArgumentException("Path is a directory"))
                : fileSystem.readFile(classSrcFile)
        ).map(file -> new ClassDepsReport(
                classSrcFile,
                StaticJavaParser.parse(file.toString()).findAll(ClassOrInterfaceType.class).stream()
                        .map(ClassOrInterfaceType::getNameAsString)
                        .toList()
        ));
    }

    /**
     * Get the dependencies of a package.
     * @param packageSrcFolder the path of the package to analyze
     * @param vertx the Vertx instance
     * @return the package dependencies list
     */
    public static Future<PackageDepsReport> getPackageDependencies(String packageSrcFolder, Vertx vertx) {
        try (Stream<Path> pathStream = Files.walk(Paths.get(packageSrcFolder))) {
            List<Future<ClassDepsReport>> classFutures = pathStream
                .filter(path -> path.toString().endsWith(".java"))
                .map(path -> getClassDependencies(path.toString(), vertx))
                .toList();
            return Future.all(classFutures).compose(allResults -> {
                List<ClassDepsReport> reports = allResults.result().list();
                return Future.succeededFuture(new PackageDepsReport(packageSrcFolder, reports));
            });
        } catch (IOException e) {
            return Future.failedFuture(e);
        }
    }

    /**
     * Get the dependencies of a project.
     * @param projectSrcFolder the path of the project to analyze
     * @param vertx the Vertx instance
     * @return the project dependencies list
     */
    public static Future<ProjectDepsReport> getProjectDependencies(String projectSrcFolder, Vertx vertx) {
        try (Stream<Path> pathStream = Files.walk(Paths.get(projectSrcFolder))) {
            List<Future<PackageDepsReport>> packageFutures = pathStream
                .filter(Files::isDirectory)
                .filter(path -> !projectSrcFolder.equals(path + File.separator))
                .map(path -> getPackageDependencies(path.toString(), vertx))
                .toList();
            return Future.all(packageFutures).compose(allResults -> {
                List<PackageDepsReport> reports = allResults.result().list();
                return Future.succeededFuture(new ProjectDepsReport(projectSrcFolder, reports));
            });
        } catch (IOException e) {
            return Future.failedFuture(e);
        }
    }

}
