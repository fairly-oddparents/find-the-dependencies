package pcd.ass02.dependencyAnalyserLib.api;

import io.vertx.core.Future;
import pcd.ass02.dependencyAnalyserLib.ClassDepsReport;
import pcd.ass02.dependencyAnalyserLib.PackageDepsReport;
import pcd.ass02.dependencyAnalyserLib.ProjectDepsReport;

public interface DependencyAnalyserLib {

    /**
     * Get the dependencies of a class.
     * @param source the class file to analyze
     * @return the class dependencies list
     */
    Future<ClassDepsReport> getClassDependencies(String source);

    /**
     * Get the dependencies of a package.
     * @param source the path of the package to analyze
     * @return the package dependencies list
     */
    Future<PackageDepsReport> getPackageDependencies(String source);

    /**
     * Get the dependencies of a project.
     * @param source the path of the project to analyze
     * @return the project dependencies list
     */
    Future<ProjectDepsReport> getProjectDependencies(String source);
}
