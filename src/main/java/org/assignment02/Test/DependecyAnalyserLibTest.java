package org.assignment02.Test;

import io.vertx.core.Vertx;
import io.vertx.core.Future;
import org.assignment02.DependencyAnalyserLib.ClassDepsReport;
import org.assignment02.DependencyAnalyserLib.DependencyAnalyserLib;
import org.assignment02.DependencyAnalyserLib.PackageDepsReport;
import org.assignment02.DependencyAnalyserLib.ProjectDepsReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DependencyAnalyserLibTest {

    @Test
    public void testGetClassDependencies() {
        Vertx vertx = Vertx.vertx();
        DependencyAnalyserLib analyser = new DependencyAnalyserLib(vertx);
        String path = System.getProperty("user.dir");
        path = path  + "\\src\\main\\java\\org\\assignment02\\";
        String classSrcFile = path + "DependencyAnalyserLib\\ClassDepsReport.java";
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
        path = path  + "\\src\\main\\java\\org\\assignment02\\";
        String packageSrcFolder = path + "DependencyAnalyserLib";
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
        path = path  + "\\src\\main\\java\\org\\assignment02\\";
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
