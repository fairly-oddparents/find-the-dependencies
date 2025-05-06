package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import java.io.File;

public class DependencyAnalyserVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        String projectPath = System.getProperty("user.dir") +
            File.separator + "src" +
            File.separator + "main" +
            File.separator + "java" +
            File.separator + "pcd" +
            File.separator + "ass02" +
            File.separator;
        String packagePath = projectPath + "dependencyAnalyser" + File.separator;
        String classSrcFile = packagePath + "Controller.java";

        DependencyAnalyserLib.getClassDependencies(classSrcFile, this.vertx)
            .onSuccess(classReport ->
                System.out.println("Class report: " + classReport.toString()))
            .onFailure(error ->
                System.err.println("Error in class dependencies: " + error));

        DependencyAnalyserLib.getPackageDependencies(packagePath, this.vertx)
            .onSuccess(packageReport ->
                System.out.println("Package report: " + packageReport.toString()))
            .onFailure(error ->
                System.err.println("Error in package dependencies: " + error));

        DependencyAnalyserLib.getProjectDependencies(projectPath, this.vertx)
            .onSuccess(projectReport ->
                System.out.println("Project report: " + projectReport.toString()))
            .onFailure(error ->
                System.err.println("Error in project dependencies: " + error))
            .onComplete(v -> vertx.close());
        startPromise.complete();
    }

}
