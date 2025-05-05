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
        sb.append(indentString)
                .append("Source: ")
                .append(source)
                .append("\n")
                .append(indentString)
                .append("Dependencies {\n");
        if (this.dependencies == null || this.dependencies.isEmpty()) {
            return sb.append(indentString).append("(None)\n").toString();
        } else {
            this.dependencies.forEach(dependency -> {
                if (dependency instanceof DepsReport<?>) {
                    sb.append(((DepsReport<?>) dependency).toString(indent + 1));
                } else {
                    sb.append(indentString).append("\t").append(dependency.toString()).append("\n");
                }
            });
        }
        return sb.append(indentString).append("}\n").toString();
    }

    @Override
    public String toString() {
        return toString(0);
    }

}
