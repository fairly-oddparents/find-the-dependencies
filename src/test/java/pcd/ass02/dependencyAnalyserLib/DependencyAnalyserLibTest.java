package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.Vertx;
import io.vertx.core.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pcd.ass02.dependencyAnalyserLib.api.DependencyAnalyserLib;
import pcd.ass02.dependencyAnalyserLib.reports.ClassDepsReport;
import pcd.ass02.dependencyAnalyserLib.impl.DependencyAnalyserLibImpl;
import pcd.ass02.dependencyAnalyserLib.reports.PackageDepsReport;
import pcd.ass02.dependencyAnalyserLib.reports.ProjectDepsReport;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class DependencyAnalyserLibTest {

    private DependencyAnalyserLib analyser;
    private String path;

    @BeforeEach
    public void setUp() {
        Vertx vertx = Vertx.vertx();
        this.analyser = new DependencyAnalyserLibImpl(vertx);
        this.path = System.getProperty("user.dir") +
                File.separator + "src" +
                File.separator + "main" +
                File.separator + "java" +
                File.separator + "pcd" +
                File.separator + "ass02" +
                File.separator;
    }

    @Test
    public void testGetClassDependencies() {
        String classSrcFile = this.path + "dependencyAnalyserLib" +
                File.separator + "reports" +
                File.separator + "ClassDepsReport.java";
        Future<ClassDepsReport> future = this.analyser.getClassDependencies(classSrcFile);
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
        String packageSrcFolder = this.path + "dependencyAnalyserLib";
        Future<PackageDepsReport> future = this.analyser.getPackageDependencies(packageSrcFolder);
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
        String projectSrcFolder = this.path;
        Future<ProjectDepsReport> future = this.analyser.getProjectDependencies(projectSrcFolder);
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
