package pcd.ass02.asynchronous.report;

import java.util.List;

/*
 * Classe che rappresenta il report delle dipendenze di un Project
 */
public class ProjectDepsReport extends DepsReport<PackageDepsReport> {

    public ProjectDepsReport(String source, List<PackageDepsReport> dependencies) {
        super(source, dependencies);
    }

}