package pcd.ass02.dependencyAnalyserLib.report;

import java.util.List;

public class DepsReport<T> {

    private final String source;
    private final List<T> dependencies;

    public DepsReport(String source, List<T> dependencies) {
        this.source = source;
        this.dependencies = dependencies;
    }

    public List<T> getDependencies() {
        return this.dependencies;
    }

    public String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String indentString = "\t".repeat(indent);
        sb.append(indentString).append("Source: ").append(source).append("\n");

        if (!this.dependencies.isEmpty() && this.dependencies.get(0) instanceof DepsReport<?>) {
            this.dependencies.forEach(dep -> {
                String toString = ((DepsReport<?>) dep).toString(indent + 1);
                sb.append(toString);
            });
        } else {
            sb.append(indentString).append("Dependencies: ").append(dependencies).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(0);
    }

}
