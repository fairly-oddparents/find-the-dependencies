package pcd.ass02.dependencyAnalyserLib.report;

import java.util.List;

/**
 * Classe che rappresenta un report delle dipendenze di una classe
 */
public class ClassDepsReport extends DepsReport<String> {

    public ClassDepsReport(String source, List<String> dependencies) {
        super(source, dependencies);
    }

}
