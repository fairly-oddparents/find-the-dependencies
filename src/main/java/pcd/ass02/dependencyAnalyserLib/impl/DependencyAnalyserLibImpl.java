package pcd.ass02.dependencyAnalyserLib.impl;

import io.vertx.core.*;
import io.vertx.core.Future;
import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import pcd.ass02.dependencyAnalyserLib.api.DependencyAnalyserLib;
import pcd.ass02.dependencyAnalyserLib.reports.ClassDepsReport;
import pcd.ass02.dependencyAnalyserLib.reports.PackageDepsReport;
import pcd.ass02.dependencyAnalyserLib.reports.ProjectDepsReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DependencyAnalyserLibImpl extends AbstractVerticle implements DependencyAnalyserLib {

    private final Vertx vertx;

    public DependencyAnalyserLibImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        System.out.println("DependencyAnalyserLib avviato");
        startPromise.complete();
    }

    @Override
    public Future<ClassDepsReport> getClassDependencies(String classSrcFile) {
        return vertx.executeBlocking(() -> {
                String code = new String(Files.readAllBytes(Paths.get(classSrcFile)));
                CompilationUnit cu = StaticJavaParser.parse(code);
                List<String> dependencies = cu.findAll(ClassOrInterfaceType.class).stream()
                    .map(ClassOrInterfaceType::getNameAsString)
                    .distinct()
                    .collect(Collectors.toList());
                return new ClassDepsReport(classSrcFile, dependencies);
        });
    }

    @Override
    public Future<PackageDepsReport> getPackageDependencies(String packageSrcFolder) {
        try (Stream<Path> stream = Files.walk(Paths.get(packageSrcFolder))) {
            List<Future<ClassDepsReport>> classFutures = stream
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> getClassDependencies(path.toString()))
                    .collect(Collectors.toList());
            return Future.all(classFutures).compose(allResults -> {
                List<ClassDepsReport> reports = allResults.result().list()
                        .stream()
                        .map(result -> (ClassDepsReport) result)
                        .collect(Collectors.toList());
                return Future.succeededFuture(new PackageDepsReport(packageSrcFolder, reports));
            });
        } catch (IOException e) {
            return Future.failedFuture(e);
        }
    }

    @Override
    public Future<ProjectDepsReport> getProjectDependencies(String projectSrcFolder) {
        try (Stream<Path> stream = Files.walk(Paths.get(projectSrcFolder))) {
            List<Future<PackageDepsReport>> packageFutures = stream
                    .filter(Files::isDirectory)
                    .map(path -> getPackageDependencies(path.toString()))
                    .collect(Collectors.toList());
            return Future.all(packageFutures).compose(allResults -> {
                List<PackageDepsReport> reports = allResults.result().list()
                        .stream()
                        .map(result -> (PackageDepsReport) result)
                        .collect(Collectors.toList());
                return Future.succeededFuture(new ProjectDepsReport(reports));
            }).andThen((v) -> {
                vertx.close();
            });
        } catch (IOException e) {
            return Future.failedFuture(e);
        }
    }

}
