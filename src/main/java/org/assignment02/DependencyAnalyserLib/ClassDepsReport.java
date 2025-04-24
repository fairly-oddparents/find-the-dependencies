package org.assignment02.DependencyAnalyserLib;

import java.util.List;

/**
 * Classe che rappresenta un report delle dipendenze di una classe
 */
public class ClassDepsReport {
    private final String sourceFileName;
    private final List<String> dependencies;

    /**
     * Costruttore della classe ClassDepsReport.
     *
     * @param sourceFileName Nome del file sorgente
     * @param dependencies   Lista delle dipendenze
     */
    public ClassDepsReport(String sourceFileName, List<String> dependencies) {
        this.sourceFileName = sourceFileName;
        this.dependencies = dependencies;
    }

    /*
     * Ritorna il nome del file sorgente
     * @return Nome del file sorgente
     */
    public String getSourceFileName() {
        return sourceFileName;
    }

    /*
     * Ritorna la lista delle dipendenze
     * @return Lista delle dipendenze
     */
    public List<String> getDependencies() {
        return dependencies;
    }

    /*
     * Ritorna una rappresentazione in stringa del report delle dipendenze
     * @return Stringa che rappresenta il report delle dipendenze
     */
    @Override
    public String toString() {
        return "ClassDepsReport{" +
            "sourceFileName='" + sourceFileName + '\'' +
            ", dependencies=" + dependencies +
            '}';
    }
}
