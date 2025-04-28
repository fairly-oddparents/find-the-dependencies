package pcd.ass02.test;

import io.vertx.core.Vertx;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;
import pcd.ass02.dependencyAnalyserLib.ClassDepsReport;
import pcd.ass02.dependencyAnalyserLib.DependencyAnalyserLib;
import pcd.ass02.dependencyAnalyserLib.PackageDepsReport;
import pcd.ass02.dependencyAnalyserLib.ProjectDepsReport;

import static org.junit.jupiter.api.Assertions.*;

class DependencyAnalyserLibTest {

    @Test
    public void testGetClassDependencies() {
        Vertx vertx = Vertx.vertx();
        DependencyAnalyserLib analyser = new DependencyAnalyserLib(vertx);
        String path = System.getProperty("user.dir");
        path = path  + "\\src\\main\\java\\pcd\\ass02\\";
        String classSrcFile = path + "dependencyAnalyserLib\\ClassDepsReport.java";
        Future<ClassDepsReport> future = analyser.getClassDependencies(classSrcFile);
        future.onComplete(res -> {
            if (res.succeeded()) {
                assertNotNull(res.result());
                assertFalse(res.result().getDependencies().isEmpty());
            } else {
                fail("Errore nell'analisi della classe: " + res.cause());
            }
        });
    }

    @Test
    public void testGetPackageDependencies() {
        Vertx vertx = Vertx.vertx();
        DependencyAnalyserLib analyser = new DependencyAnalyserLib(vertx);
        String path = System.getProperty("user.dir");
        path = path  + "\\src\\main\\java\\pcd\\ass02\\";
        String packageSrcFolder = path + "dependencyAnalyserLib";
        Future<PackageDepsReport> future = analyser.getPackageDependencies(packageSrcFolder);
        future.onComplete(res -> {
            if (res.succeeded()) {
                assertNotNull(res.result());
                assertFalse(res.result().getClassReports().isEmpty());
            } else {
                fail("Errore nell'analisi della classe: " + res.cause());
            }
        });
    }

    @Test
    public void testGetProjectDependencies() {
        Vertx vertx = Vertx.vertx();
        DependencyAnalyserLib analyser = new DependencyAnalyserLib(vertx);
        String path = System.getProperty("user.dir");
        path = path  + "\\src\\main\\java\\pcd\\ass02\\";
        String projectSrcFolder = path;
        Future<ProjectDepsReport> future = analyser.getProjectDependencies(projectSrcFolder);
        future.onComplete(res -> {
            if (res.succeeded()) {
                assertNotNull(res.result());
                assertFalse(res.result().getPackageReports().isEmpty());
            } else {
                fail("Errore nell'analisi della classe: " + res.cause());
            }
        });
    }
}
