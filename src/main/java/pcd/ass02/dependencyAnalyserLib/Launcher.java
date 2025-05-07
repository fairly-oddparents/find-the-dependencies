package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.io.File;

public class Launcher {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new AbstractVerticle() {

            @Override
            public void start(Promise<Void> startPromise) {
                String projectPath = System.getProperty("user.dir")
                        + File.separator + "src";
                String packagePath = projectPath
                        + File.separator + "main"
                        + File.separator + "java"
                        + File.separator + "pcd"
                        + File.separator + "ass02"
                        + File.separator + "dependencyAnalyser";
                String classSrcFile = packagePath
                        + File.separator + "Controller.java";

                DependencyAnalyserLib.getClassDependencies(classSrcFile, this.vertx)
                        .onSuccess(classReport ->
                                System.out.println("[CLASS REPORT]:\n" + classReport.toString()))
                        .onFailure(error ->
                                System.err.println("Error in class dependencies: " + error));

                DependencyAnalyserLib.getPackageDependencies(packagePath, this.vertx)
                        .onSuccess(packageReport ->
                                System.out.println("[PACKAGE REPORT]:\n" + packageReport.toString()))
                        .onFailure(error ->
                                System.err.println("Error in package dependencies: " + error));

                DependencyAnalyserLib.getProjectDependencies(projectPath, this.vertx)
                        .onSuccess(projectReport ->
                                System.out.println("[PROJECT REPORT]:\n" + projectReport.toString()))
                        .onFailure(error ->
                                System.err.println("Error in project dependencies: " + error))
                        .onComplete(v -> vertx.close());

                startPromise.complete();
            }
        });
    }

}
