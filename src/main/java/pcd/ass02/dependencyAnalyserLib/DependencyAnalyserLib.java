package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
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

    public DependencyAnalyserLib() {
    }

    /**
     * Get the dependencies of a class.
     * @param source the class file to analyze
     * @return the class dependencies list
     */
    public static Future<ClassDepsReport> getClassDependencies(String source) {
        return Vertx.vertx().executeBlocking(() -> {
            String code = new String(Files.readAllBytes(Paths.get(source)));
            List<String> dependencies = StaticJavaParser.parse(code).findAll(ClassOrInterfaceType.class).stream()
                    .map(ClassOrInterfaceType::getNameAsString)
                    .distinct()
                    .toList();
            return new ClassDepsReport(source, dependencies);
        });
    }

    /**
     * Get the dependencies of a package.
     * @param source the path of the package to analyze
     * @return the package dependencies list
     */
    public static Future<PackageDepsReport> getPackageDependencies(String source) {
        try (Stream<Path> pathStream = Files.walk(Paths.get(source))) {
            List<Future<ClassDepsReport>> classFutures = pathStream
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> getClassDependencies(path.toString()))
                    .toList();
            return Future.all(classFutures).compose(allResults -> {
                List<ClassDepsReport> reports = allResults.result().list();
                return Future.succeededFuture(new PackageDepsReport(source, reports));
            });
        } catch (IOException e) {
            return Future.failedFuture(e);
        }
    }

    /**
     * Get the dependencies of a project.
     * @param source the path of the project to analyze
     * @return the project dependencies list
     */
    public static Future<ProjectDepsReport> getProjectDependencies(String source) {
        try (Stream<Path> pathStream = Files.walk(Paths.get(source))) {
            List<Future<PackageDepsReport>> packageFutures = pathStream
                    .filter(Files::isDirectory)
                    .filter(path -> !source.equals(path + File.separator))
                    .map(path -> getPackageDependencies(path.toString()))
                    .toList();
            return Future.all(packageFutures).compose(allResults -> {
                List<PackageDepsReport> reports = allResults.result().list();
                return Future.succeededFuture(new ProjectDepsReport(source, reports));
            });
        } catch (IOException e) {
            return Future.failedFuture(e);
        }
    }

}
