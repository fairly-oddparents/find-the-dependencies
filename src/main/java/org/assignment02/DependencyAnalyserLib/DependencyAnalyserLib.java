package org.assignment02.DependencyAnalyserLib;

import io.vertx.core.*;
import io.vertx.core.Future;
import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DependencyAnalyserLib extends AbstractVerticle {

    private final Vertx vertx;

    public DependencyAnalyserLib(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        System.out.println("DependencyAnalyserLib avviato");
        startPromise.complete();
    }

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

    public Future<PackageDepsReport> getPackageDependencies(String packageSrcFolder) {
        return vertx.executeBlocking(promise -> {
            try {
                List<Future> classFutures = new ArrayList<>();
                Files.walk(Paths.get(packageSrcFolder))
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(classFile -> classFutures.add(getClassDependencies(classFile.toString())));

                CompositeFuture.all(classFutures).onComplete(allResults -> {
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

    public Future<ProjectDepsReport> getProjectDependencies(String projectSrcFolder) {
        return vertx.executeBlocking(promise -> {
            try {
                List<Future> packageFutures = new ArrayList<>();
                Files.walk(Paths.get(projectSrcFolder))
                    .filter(Files::isDirectory)
                    .forEach(packageFolder -> packageFutures.add(getPackageDependencies(packageFolder.toString())));

                CompositeFuture.all(packageFutures).onComplete(allResults -> {
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

    public static void main(String[] args) {
        String path = System.getProperty("user.dir");
        path = path  + "\\src\\main\\java\\org\\assignment02\\";
        String classSrcFile = path + "DependencyAnalyserLib\\ClassDepsReport.java";
        String packageSrcFolder = path + "DependencyAnalyserLib";
        String projectSrcFolder = path;
        Vertx vertx = Vertx.vertx();
        DependencyAnalyserLib analyser = new DependencyAnalyserLib(vertx);

        vertx.deployVerticle(analyser, res -> {
            if (res.succeeded()) {
                analyser.getClassDependencies(classSrcFile).onComplete(classRes -> {
                    if (classRes.succeeded()) {
                        System.out.println("Dependencies of: " + classSrcFile);
                        System.out.println(" > " + classRes.result().getDependencies());
                    } else {
                        System.err.println("Errore: " + classRes.cause());
                    }
                });
                analyser.getPackageDependencies(packageSrcFolder).onComplete(packageRes -> {
                    if (packageRes.succeeded()) {
                        System.out.println("Package Dependencies:");
                        packageRes.result().getClassReports().forEach(classReport -> {
                            System.out.println("- File: " + classReport.getSourceFileName());
                            System.out.println("  Dependencies: " + classReport.getDependencies());
                        });
                    } else {
                        System.err.println("Errore nell'analisi del package: " + packageRes.cause());
                    }
                });
                analyser.getProjectDependencies(projectSrcFolder).onComplete(projectRes -> {
                    if (projectRes.succeeded()) {
                        System.out.println("Project Dependencies:");
                        projectRes.result().getPackageReports().forEach(packageReport -> {
                            System.out.println("- Package: " + packageReport.getPackageName());
                            packageReport.getClassReports().forEach(classReport -> {
                                System.out.println("  - File: " + classReport.getSourceFileName());
                                System.out.println("    Dependencies: " + classReport.getDependencies());
                            });
                        });
                    } else {
                        System.err.println("Errore nell'analisi del progetto: " + projectRes.cause());
                    }
                });
            } else {
                System.err.println("Errore nella distribuzione del Vertical: " + res.cause());
            }
        });
    }

}
