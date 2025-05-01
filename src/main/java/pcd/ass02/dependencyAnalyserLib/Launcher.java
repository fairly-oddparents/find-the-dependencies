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
        String packagePath = projectPath + "p2" + File.separator ;
        String classSrcFile = packagePath + "B.java";

        vertx.deployVerticle(analyser, res -> {
            if (!res.succeeded()) {
                System.err.println("Error deploying: " + res.cause());
            } else {
                analyser.getClassDependencies(classSrcFile)
                        .onSuccess(classReport -> System.out.println(classReport.toString()))
                        .onFailure(error -> System.err.println("Error: " + error));

                analyser.getPackageDependencies(packagePath)
                        .onSuccess(packageReport -> System.out.println(packageReport.toString()))
                        .onFailure(error -> System.err.println("Error: " + error));

                analyser.getProjectDependencies(projectPath)
                        .onSuccess(projectReport -> System.out.println(projectReport.toString()))
                        .onFailure(error -> System.err.println("Error: " + error))
                        .onComplete(v -> vertx.close());
            }
        });
    }
}
