package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.Vertx;

public class Launcher {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DependencyAnalyserVerticle());
    }
}
