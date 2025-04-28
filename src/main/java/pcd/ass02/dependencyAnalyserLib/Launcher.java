package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.Vertx;
import pcd.ass02.dependencyAnalyserLib.impl.DependencyAnalyserLibImpl;

public class Launcher {
    public static void main(String[] args) {
        String path = System.getProperty("user.dir");
        path = path  + "\\src\\main\\java\\pcd\\ass02\\";
        String classSrcFile = path + "dependencyAnalyserLib\\reports\\ClassDepsReport.java";
        String packageSrcFolder = path + "dependencyAnalyserLib";
        String projectSrcFolder = path;
        Vertx vertx = Vertx.vertx();
        DependencyAnalyserLibImpl analyser = new DependencyAnalyserLibImpl(vertx);

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
