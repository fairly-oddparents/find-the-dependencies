package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.Vertx;

import java.io.File;

public class Launcher {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        String projectPath = System.getProperty("user.dir") +
                File.separator + "src" +
                File.separator + "test" +
                File.separator + "resources" +
                File.separator + "test-project" +
                File.separator;
        String packagePath = projectPath + "p2" + File.separator ;
        String classSrcFile = packagePath + "B.java";

        DependencyAnalyserLib.getClassDependencies(classSrcFile)
                .onSuccess(classReport -> System.out.println(classReport.toString()))
                .onFailure(error -> System.err.println("Error: " + error));

        DependencyAnalyserLib.getPackageDependencies(packagePath)
                .onSuccess(packageReport -> System.out.println(packageReport.toString()))
                .onFailure(error -> System.err.println("Error: " + error));

        DependencyAnalyserLib.getProjectDependencies(projectPath)
                .onSuccess(projectReport -> System.out.println(projectReport.toString()))
                .onFailure(error -> System.err.println("Error: " + error))
                .onComplete(v -> {
                    vertx.close(handler -> {
                        System.exit(0);
                    });
                });
    }
}
