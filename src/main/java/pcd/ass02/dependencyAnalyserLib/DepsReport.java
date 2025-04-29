package pcd.ass02.dependencyAnalyserLib;

import java.util.List;

public class DepsReport<T> {

    private final String source;
    private final List<T> dependencies;

    public DepsReport(String source, List<T> dependencies) {
        this.source = source;
        this.dependencies = dependencies;
    }

    public String getSource() {
        return this.source;
    }

    public List<T> getDependencies() {
        return this.dependencies;
    }

    @Override
    public String toString() {
        return "DepsReport{" +
                "source='" + source + '\'' +
                ", dependencies=" + dependencies +
                '}';
    }

}
