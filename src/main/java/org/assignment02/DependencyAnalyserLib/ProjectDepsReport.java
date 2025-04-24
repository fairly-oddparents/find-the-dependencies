package org.assignment02.DependencyAnalyserLib;

import java.util.List;

/*
 * Classe che rappresenta il report delle dipendenze di un Project
 */
public class ProjectDepsReport {

    private final List<PackageDepsReport> packageReports;

    /*
     * Costruttore della classe ProjectDepsReport
     * @param packageReports Lista dei report dei package
     */
    public ProjectDepsReport(List<PackageDepsReport> packageReports) {
        this.packageReports = packageReports;
    }

    /*
     * Ritorna i report dei package
     * @return Lista dei report dei package
     */
    public List<PackageDepsReport> getPackageReports() {
        return packageReports;
    }

    /*
     * Ritorna una rappresentazione in stringa del report delle dipendenze del progetto
     * @return Stringa che rappresenta il report delle dipendenze del progetto
     */
    @Override
    public String toString() {
        return "ProjectDepsReport{" +
            "packageReports=" + packageReports +
            '}';
    }
}
