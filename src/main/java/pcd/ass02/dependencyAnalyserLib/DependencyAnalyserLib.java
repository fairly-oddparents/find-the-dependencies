package pcd.ass02.dependencyAnalyserLib;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import io.vertx.core.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.vertx.core.file.FileSystem;
import pcd.ass02.dependencyAnalyserLib.report.ClassDepsReport;
import pcd.ass02.dependencyAnalyserLib.report.PackageDepsReport;
import pcd.ass02.dependencyAnalyserLib.report.ProjectDepsReport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DependencyAnalyserLib {

    public static final String JAVA_EXTENSION = ".java";

    public DependencyAnalyserLib() { }

    /**
     * Get the dependencies of a class.
     *
     * @param classSrcFile the class file to analyze
     * @param vertx        the Vertx instance
     * @return the class dependencies list
     */
    public static Future<ClassDepsReport> getClassDependencies(String classSrcFile, Vertx vertx) {
        FileSystem fileSystem = vertx.fileSystem();
        if (!classSrcFile.endsWith(JAVA_EXTENSION)) {
            throw new IllegalArgumentException("File is not a Java file");
        }
        return fileSystem.exists(classSrcFile)
            .compose(exists -> exists
                ? fileSystem.lprops(classSrcFile)
                : Future.failedFuture(new IllegalArgumentException("File not found")))
            .compose(fileProps -> fileProps.isDirectory()
                ? Future.failedFuture(new IllegalArgumentException("Path is a directory"))
                : fileSystem.readFile(classSrcFile))
            .map(file -> {
                CompilationUnit cu = StaticJavaParser.parse(file.toString());
                Set<String> excludedNames = getExcludedNames(cu);
                Set<String> dependencySet = new HashSet<>();
                dependencySet.addAll(getTypeDependencies(cu, excludedNames));
                dependencySet.addAll(getMethodCallDependencies(cu, excludedNames));
                dependencySet.addAll(getFieldAccessDependencies(cu, excludedNames));
                List<String> dependencies = new ArrayList<>(dependencySet);
                Collections.sort(dependencies);
                return new ClassDepsReport(classSrcFile, dependencies);
            });
    }

    /**
     * Estrae i nomi da escludere, cioè quelli dichiarati come variabili e parametri.
     *
     * @param cu il CompilationUnit
     * @return un set di nomi da escludere
     */
    private static Set<String> getExcludedNames(CompilationUnit cu) {
        Set<String> excluded = new HashSet<>();
        excluded.addAll(cu.findAll(VariableDeclarator.class).stream()
            .map(VariableDeclarator::getNameAsString)
            .collect(Collectors.toSet()));
        excluded.addAll(cu.findAll(Parameter.class).stream()
            .map(Parameter::getNameAsString)
            .collect(Collectors.toSet()));
        return excluded;
    }

    /**
     * Estrae le dipendenze dai nodi di tipo ClassOrInterfaceType.
     *
     * @param cu            il CompilationUnit
     * @param excludedNames i nomi da escludere
     * @return un set di nomi di dipendenze
     */
    private static Set<String> getTypeDependencies(CompilationUnit cu, Set<String> excludedNames) {
        return cu.findAll(ClassOrInterfaceType.class).stream()
            .map(ClassOrInterfaceType::getNameAsString)
            .filter(name -> !excludedNames.contains(name))
            .collect(Collectors.toSet());
    }

    /**
     * Estrae le dipendenze dal qualificatore nelle chiamate ai metodi.
     *
     * @param cu            il CompilationUnit
     * @param excludedNames i nomi da escludere
     * @return un set di nomi estratti dai qualifier dei MethodCallExpr
     */
    private static Set<String> getMethodCallDependencies(CompilationUnit cu, Set<String> excludedNames) {
        return cu.findAll(MethodCallExpr.class).stream()
            .map(MethodCallExpr::getScope)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(expr -> expr instanceof NameExpr)
            .map(expr -> ((NameExpr) expr).getNameAsString())
            .filter(name -> !excludedNames.contains(name))
            .collect(Collectors.toSet());
    }

    /**
     * Estrae le dipendenze dal qualificatore negli accessi ai campi.
     *
     * @param cu            il CompilationUnit
     * @param excludedNames i nomi da escludere
     * @return un set di nomi estratti dai FieldAccessExpr
     */
    private static Set<String> getFieldAccessDependencies(CompilationUnit cu, Set<String> excludedNames) {
        return cu.findAll(FieldAccessExpr.class).stream()
            .map(FieldAccessExpr::getScope)
            .filter(expr -> expr instanceof NameExpr)
            .map(expr -> ((NameExpr) expr).getNameAsString())
            .filter(name -> !excludedNames.contains(name))
            .collect(Collectors.toSet());
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
                .filter(file -> file.endsWith(JAVA_EXTENSION))
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
        try (Stream<Path> pathStream = Files.walk(Paths.get(projectSrcFolder))) {
            List<Future<PackageDepsReport>> packageFutures = pathStream
                .filter(Files::isDirectory)
                .filter(path -> !projectSrcFolder.equals(path + File.separator))
                .map(path -> getPackageDependencies(path.toString(), vertx))
                .toList();
            return Future.all(packageFutures).compose(allResults -> {
                List<PackageDepsReport> reports = allResults.result().list();
                return Future.succeededFuture(new ProjectDepsReport(projectSrcFolder, reports));
            });
        } catch (IOException e) {
            return Future.failedFuture(e);
        }
    }

}
