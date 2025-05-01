package pcd.ass02.dependencyAnalyserLib;

import java.util.List;

/*
 * Classe che rappresenta il report delle dipendenze di un Package
 */
public class PackageDepsReport extends DepsReport<ClassDepsReport> {

    public PackageDepsReport(String source, List<ClassDepsReport> dependencies) {
        super(source, dependencies);
    }

}
