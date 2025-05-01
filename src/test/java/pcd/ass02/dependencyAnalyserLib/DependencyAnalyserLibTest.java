package pcd.ass02.dependencyAnalyserLib;

import io.vertx.core.Vertx;
import io.vertx.core.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import pcd.ass02.dependencyAnalyserLib.api.DependencyAnalyserLib;

import static org.junit.jupiter.api.Assertions.*;

class DependencyAnalyserLibTest {

    private DependencyAnalyserLib analyser;
    private CompletableFuture<Void> testResult;
    private String path;

    @BeforeEach
    public void setUp() {
        this.analyser = new DependencyAnalyserLibImpl(Vertx.vertx());
        this.testResult = new CompletableFuture<>();
        this.path = System.getProperty("user.dir") +
                File.separator + "src" +
                File.separator + "test" +
                File.separator + "resources" +
                File.separator + "test-project" +
                File.separator;
    }

    @Test
    public void testGetClassDependencies() {
        String classSrcFile = this.path + "p2" + File.separator + "B.java";
        Future<ClassDepsReport> future = this.analyser.getClassDependencies(classSrcFile);
        future.onComplete(res -> {
            try {
                if (res.succeeded()) {
                    assertNotNull(res.result());
                    assertFalse(res.result().getDependencies().isEmpty());
                    assertEquals(Set.of("A"), new HashSet<>(res.result().getDependencies()));
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
        String packageSrcFolder = this.path + "p2";
        Future<PackageDepsReport> future = this.analyser.getPackageDependencies(packageSrcFolder);
        future.onComplete(res -> {
            try {
                if (res.succeeded()) {
                    assertNotNull(res.result());
                    assertFalse(res.result().getDependencies().isEmpty());
                    assertEquals(
                            Set.of("A"),
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
        Future<ProjectDepsReport> future = this.analyser.getProjectDependencies(projectSrcFolder);
        future.onComplete(res -> {
            try {
                if (res.succeeded()) {
                    assertNotNull(res.result());
                    assertFalse(res.result().getDependencies().isEmpty());
                    assertEquals(
                            Set.of("A"),
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
