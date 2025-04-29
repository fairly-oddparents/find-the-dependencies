package pcd.ass02.dependencyAnalyserLib.api;

import io.vertx.core.Future;
import pcd.ass02.dependencyAnalyserLib.ClassDepsReport;
import pcd.ass02.dependencyAnalyserLib.PackageDepsReport;
import pcd.ass02.dependencyAnalyserLib.ProjectDepsReport;

public interface DependencyAnalyserLib {

    /**
     * Get the dependencies of a class
     * @param classSrcFile the class file to analyze
     * @return the class dependencies list
     */
    Future<ClassDepsReport> getClassDependencies(String classSrcFile);

    /**
     * Get the dependencies of a package
     * @param packageSrcFolder the path of the package to analyze
     * @return the package dependencies list
     */
    Future<PackageDepsReport> getPackageDependencies(String packageSrcFolder);

    /**
     * Get the dependencies of a project
     * @param projectSrcFolder the path of the project to analyze
     * @return the project dependencies list
     */
    Future<ProjectDepsReport> getProjectDependencies(String projectSrcFolder);
}
