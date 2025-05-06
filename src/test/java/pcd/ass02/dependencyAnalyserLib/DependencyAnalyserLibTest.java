package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pcd.ass02.dependencyAnalyserLib.report.ClassDepsReport;
import pcd.ass02.dependencyAnalyserLib.report.PackageDepsReport;
import pcd.ass02.dependencyAnalyserLib.report.ProjectDepsReport;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DependencyAnalyserLibTest {

    private CompletableFuture<Void> testResult;
    private String path;

    @BeforeEach
    public void setUp() {
        this.testResult = new CompletableFuture<>();
        this.path = System.getProperty("user.dir") +
                File.separator + "src" +
                File.separator + "test" +
                File.separator + "resources" +
                File.separator + "test-project" +
                File.separator + "src";
    }

    @Test
    public void testGetClassDependencies() {
        String classSrcFile = this.path
                + File.separator + "main"
                + File.separator + "java"
                + File.separator + "example"
                + File.separator + "sub"
                + File.separator + "ClassA.java";
        Future<ClassDepsReport> future = DependencyAnalyserLib.getClassDependencies(classSrcFile, Vertx.vertx());
        future.onComplete(res -> {
            try {
                if (res.succeeded()) {
                    assertNotNull(res.result());
                    assertFalse(res.result().getDependencies().isEmpty());
                    assertEquals(Set.of("ClassB", "Helper"), new HashSet<>(res.result().getDependencies()));
                    this.testResult.complete(null);
                } else {
                    this.testResult.completeExceptionally(res.cause());
                }
            } catch (Throwable t) {
                this.testResult.completeExceptionally(t);
            }
        });
        this.testResult.join();
    }

    @Test
    public void testGetPackageDependencies() {
        String packageSrcFolder = this.path
                + File.separator + "main"
                + File.separator + "java"
                + File.separator + "example"
                + File.separator + "sub";
        Future<PackageDepsReport> future = DependencyAnalyserLib.getPackageDependencies(packageSrcFolder, Vertx.vertx());
        future.onComplete(res -> {
            try {
                if (res.succeeded()) {
                    assertNotNull(res.result());
                    assertFalse(res.result().getDependencies().isEmpty());
                    assertEquals(
                            Set.of("ClassA", "ClassB", "Helper"),
                            res.result().getDependencies().stream()
                                    .flatMap(report -> report.getDependencies().stream())
                                    .collect(Collectors.toSet())
                    );
                    this.testResult.complete(null);
                } else {
                    this.testResult.completeExceptionally(res.cause());
                }
            } catch (Throwable t) {
                this.testResult.completeExceptionally(t);
            }
        });
        this.testResult.join();
    }

    @Test
    public void testGetProjectDependencies() {
        String projectSrcFolder = this.path;
        Future<ProjectDepsReport> future = DependencyAnalyserLib.getProjectDependencies(projectSrcFolder, Vertx.vertx());
        future.onComplete(res -> {
            try {
                if (res.succeeded()) {
                    assertNotNull(res.result());
                    assertFalse(res.result().getDependencies().isEmpty());
                    assertEquals(
                            Set.of("ClassA", "ClassB", "ServiceA", "Helper"),
                            res.result().getDependencies().stream()
                                    .flatMap(report -> report.getDependencies().stream())
                                    .flatMap(report -> report.getDependencies().stream())
                                    .collect(Collectors.toSet())
                    );
                    this.testResult.complete(null);
                } else {
                    this.testResult.completeExceptionally(res.cause());
                }
            } catch (Throwable t) {
                this.testResult.completeExceptionally(t);
            }
        });
        this.testResult.join();
    }
}