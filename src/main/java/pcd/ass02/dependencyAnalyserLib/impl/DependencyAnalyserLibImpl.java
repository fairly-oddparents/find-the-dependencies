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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        return vertx.executeBlocking(promise -> {
            try {
                String code = new String(Files.readAllBytes(Paths.get(classSrcFile)));
                CompilationUnit cu = StaticJavaParser.parse(code);
                List<String> dependencies = cu.findAll(ClassOrInterfaceType.class).stream()
                    .map(ClassOrInterfaceType::getNameAsString)
                    .distinct()
                    .collect(Collectors.toList());
                ClassDepsReport report = new ClassDepsReport(classSrcFile, dependencies);

                promise.complete(report);
            } catch (Exception e) {
                promise.fail(e);
            }
        });
    }

    @Override
    public Future<PackageDepsReport> getPackageDependencies(String packageSrcFolder) {
        return vertx.executeBlocking(promise -> {
            try {
                List<Future<ClassDepsReport>> classFutures = new ArrayList<>();
                try (Stream<Path> stream = Files.walk(Paths.get(packageSrcFolder))) {
                    stream.filter(path -> path.toString().endsWith(".java"))
                            .forEach(classFile -> classFutures.add(getClassDependencies(classFile.toString())));
                }
                Future.all(classFutures).onComplete(allResults -> {
                    if (allResults.succeeded()) {
                        List<ClassDepsReport> packageDependencies = new ArrayList<>();
                        allResults.result().list().forEach(result -> packageDependencies.add((ClassDepsReport) result));

                        PackageDepsReport report = new PackageDepsReport(packageSrcFolder, packageDependencies);
                        promise.complete(report);
                    } else {
                        promise.fail(allResults.cause());
                    }
                });
            } catch (Exception e) {
                promise.fail(e);
            }
        });
    }

    @Override
    public Future<ProjectDepsReport> getProjectDependencies(String projectSrcFolder) {
        return vertx.executeBlocking(promise -> {
            try {
                List<Future<PackageDepsReport>> packageFutures = new ArrayList<>();
                try (Stream<Path> stream = Files.walk(Paths.get(projectSrcFolder))) {
                    stream.filter(Files::isDirectory)
                            .forEach(packageFolder -> packageFutures.add(getPackageDependencies(packageFolder.toString())));
                }
                Future.all(packageFutures).onComplete(allResults -> {
                    if (allResults.succeeded()) {
                        List<PackageDepsReport> projectDependencies = new ArrayList<>();
                        allResults.result().list().forEach(result -> projectDependencies.add((PackageDepsReport) result));

                        ProjectDepsReport report = new ProjectDepsReport(projectDependencies);
                        promise.complete(report);
                    } else {
                        promise.fail(allResults.cause());
                    }
                });
            } catch (Exception e) {
                promise.fail(e);
            }
        });
    }

}
