package pcd.ass02.dependencyAnalyser;

import java.util.Set;

public class ClassDependency {
    private final String className;
    private final Set<String> dependencies;

    ClassDependency(String className, Set<String> dependencies) {
        this.className = className;
        this.dependencies = dependencies;
    }

    public String getName(){
        return this.className;
    }

    public Set<String> getDependencies(){
        return this.dependencies;
    }
}
