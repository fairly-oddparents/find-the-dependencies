package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.Vertx;

import java.io.File;

public class Launcher {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DependencyAnalyserLibImpl analyser = new DependencyAnalyserLibImpl(vertx);
        String projectPath = System.getProperty("user.dir") +
                File.separator + "src" +
                File.separator + "test" +
                File.separator + "resources" +
                File.separator + "test-project" +
                File.separator;
        String packagePath = projectPath + "p2";
        String classSrcFile = packagePath + File.separator + "B.java";

        vertx.deployVerticle(analyser, res -> {
            if (!res.succeeded()) {
                System.err.println("Error deploying: " + res.cause());
            } else {
                analyser.getClassDependencies(classSrcFile)
                        .onSuccess(report -> {
                            System.out.println("Class: " + report.getSource());
                            System.out.println("Dependencies: " + report.getDependencies());
                        })
                        .onFailure(error -> System.err.println("Error: " + error));

                analyser.getPackageDependencies(packagePath)
                        .onSuccess(report -> {
                            System.out.println("Package: " + report.getSource());
                            report.getDependencies().forEach(classReport -> {
                                System.out.println("\tFile: " + classReport.getSource());
                                System.out.println("\tDependencies: " + classReport.getDependencies());
                            });
                        })
                        .onFailure(error -> System.err.println("Error: " + error));

                analyser.getProjectDependencies(projectPath)
                        .onSuccess(report -> {
                            System.out.println("Project: " + report.getSource());
                            report.getDependencies().forEach(packageReport -> {
                                packageReport.getDependencies().forEach(classReport -> {
                                    System.out.println("\tFile: " + classReport.getSource());
                                    System.out.println("\tDependencies: " + classReport.getDependencies());
                                });
                            });
                        })
                        .onFailure(error -> System.err.println("Error: " + error))
                        .onComplete(v -> vertx.close());
            }
        });
    }
}
