package pcd.ass02.asynchronous;

import io.vertx.core.*;
import io.vertx.core.file.FileSystem;
import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import pcd.ass02.PathHelper;
import pcd.ass02.asynchronous.report.ClassDepsReport;
import pcd.ass02.asynchronous.report.PackageDepsReport;
import pcd.ass02.asynchronous.report.ProjectDepsReport;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class DependencyAnalyserLib {

    public DependencyAnalyserLib() { }

    /**
     * Get the dependencies of a class.
     * @param classSrcFile the class file to analyze
     * @param vertx the Vertx instance
     * @return the class dependencies list
     */
    public static Future<ClassDepsReport> getClassDependencies(String classSrcFile, Vertx vertx) {
        JavaParser parser = PathHelper.getJavaParser(classSrcFile);
        FileSystem fileSystem = vertx.fileSystem();
        if (!classSrcFile.endsWith(PathHelper.FILE_EXTENSION)) {
            throw new IllegalArgumentException("Not a Java file");
        }

        return fileSystem.exists(classSrcFile).compose(exists ->
                exists ? fileSystem.lprops(classSrcFile) : Future.failedFuture("File not found")
        ).compose(props -> props.isDirectory()
                ? Future.failedFuture("Path is a directory")
                : fileSystem.readFile(classSrcFile)
        ).map(file -> {
            CompilationUnit cu = parser.parse(file.toString()).getResult()
                    .orElseThrow(() -> new RuntimeException("Parsing failed"));
            List<String> deps = PathHelper.collectDependencies(cu, Paths.get(classSrcFile));
            return new ClassDepsReport(classSrcFile, deps);
        });
    }

    /**
     * Get the dependencies of a package.
     * @param packageSrcFolder the path of the package to analyze
     * @param vertx the Vertx instance
     * @return the package dependencies list
     */
    public static Future<PackageDepsReport> getPackageDependencies(String packageSrcFolder, Vertx vertx) {
        FileSystem fileSystem = vertx.fileSystem();
        return fileSystem.exists(packageSrcFolder).compose(exists ->
                exists ? fileSystem.lprops(packageSrcFolder)
                : Future.failedFuture(new IllegalArgumentException("Path not found"))
        ).compose(dirProps ->
                !dirProps.isDirectory() ? Future.failedFuture(new IllegalArgumentException("Path is not a directory"))
                : fileSystem.readDir(packageSrcFolder)
        ).compose(files -> Future.all(files.stream()
                .filter(file -> file.endsWith(PathHelper.FILE_EXTENSION))
                .map(file -> getClassDependencies(file, vertx))
                .toList()
        )).compose(composite -> Future.succeededFuture(new PackageDepsReport(
                packageSrcFolder,
                composite.result().list()
        )));
    }

    /**
     * Get the dependencies of a project.
     * @param projectSrcFolder the path of the project to analyze
     * @param vertx the Vertx instance
     * @return the project dependencies list
     */
    public static Future<ProjectDepsReport> getProjectDependencies(String projectSrcFolder, Vertx vertx) {
        FileSystem fileSystem = vertx.fileSystem();
        return fileSystem.exists(projectSrcFolder).compose(exists -> exists
                ? fileSystem.lprops(projectSrcFolder)
                : Future.failedFuture("Path not found")
        ).compose(props -> props.isDirectory()
                ? scanForPackages(projectSrcFolder, vertx)
                : Future.failedFuture("Path is not a directory")
        ).compose(packageFolders -> {
            List<Future<PackageDepsReport>> packageDeps = packageFolders.stream()
                    .map(folder -> getPackageDependencies(folder, vertx))
                    .toList();
            return Future.all(packageDeps).map(cf ->
                    new ProjectDepsReport(projectSrcFolder, cf.list())
            );
        });
    }

    private static Future<List<String>> scanForPackages(String dir, Vertx vertx) {
        FileSystem fileSystem = vertx.fileSystem();
        return fileSystem.readDir(dir).compose(entries -> {
            List<Future<List<String>>> subCalls = new ArrayList<>();
            boolean hasJava = entries.stream().anyMatch(f -> f.endsWith(PathHelper.FILE_EXTENSION));

            for (String entry : entries) {
                subCalls.add(fileSystem.lprops(entry).compose(props ->
                        props.isDirectory() ? scanForPackages(entry, vertx)
                                : Future.succeededFuture(List.of())
                ));
            }

            return Future.all(subCalls).map(cf -> {
                List<String> result = new ArrayList<>();
                if (hasJava) result.add(dir);
                for (int i = 0; i < cf.size(); i++) {
                    result.addAll(cf.resultAt(i));
                }
                return result;
            });
        });
    }
}
