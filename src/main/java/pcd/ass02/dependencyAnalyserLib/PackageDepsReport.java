package pcd.ass02.dependencyAnalyserLib;

import java.util.List;

/*
 * Classe che rappresenta il report delle dipendenze di un Package
 */
public class PackageDepsReport {
    private String packageName = "";
    private final List<ClassDepsReport> classReports;

    /*
     * Costruttore della classe PackageDepsReport
     * @param packageSrcFolder Nome del Package sorgente
     * @param classReports Lista dei report delle classi
     */
    public PackageDepsReport(String packageSrcFolder, List<ClassDepsReport> classReports) {
        this.packageName = packageName;
        this.classReports = classReports;
    }

    /*
     * Ritorna il nome del Package
     * @return Nome del Package
     */
    public String getPackageName() {
        return packageName;
    }

    /*
     * Ritorna i report delle classi
     * @return Lista dei report delle classi
     */
    public List<ClassDepsReport> getClassReports() {
        return classReports;
    }

    /*
     * Ritorna il nome del file sorgente della classe
     * @return Nome del file sorgente della classe
     */
    @Override
    public String toString() {
        return "PackageDepsReport{" +
            "packageName='" + packageName + '\'' +
            ", classReports=" + classReports +
            '}';
    }
}
