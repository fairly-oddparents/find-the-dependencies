package org.assignment02.Gui;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;

import io.vertx.core.Vertx;
import org.graphstream.ui.view.Viewer;
import pcd.ass02.dependencyAnalyserLib.DependencyAnalyserLibImpl;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

public class DependencyAnalyser {
    private JFrame frame;
    private JTextField folderPathField;
    private JButton selectFolderButton, analyzeButton;
    private JLabel classesLabel, dependenciesLabel;
    private Vertx vertx;
    private Graph graph;
    private Viewer viewer;

    public DependencyAnalyser() {
        frame = new JFrame("Dependency Graph Analyzer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        folderPathField = new JTextField(30);
        selectFolderButton = new JButton("Scegli cartella...");
        analyzeButton = new JButton("Avvia analisi");
        topPanel.add(new JLabel("Source Root Folder:"));
        topPanel.add(folderPathField);
        topPanel.add(selectFolderButton);
        topPanel.add(analyzeButton);

        System.setProperty("org.graphstream.ui", "swing");
        graph = new MultiGraph("Dipendenze");
        graph.setAttribute("ui.stylesheet", "node { "
                + "text-size: 14px; "
                + "text-color: black; "
                + "text-background-mode: plain; "
                + "text-background-color: white; "
                + "text-padding: 5px, 3px; "
                + "text-offset: 10px, 10px; "
                + "}");
        viewer = graph.display();

        frame.add(topPanel, BorderLayout.NORTH);
        frame.setVisible(true);
        vertx = Vertx.vertx();

        selectFolderButton.addActionListener(e -> chooseFolder());
        analyzeButton.addActionListener(e -> startAnalysis());
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            folderPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void startAnalysis() {
        String folderPath = folderPathField.getText();
        if (folderPath.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Seleziona una cartella prima di avviare l'analisi!");
            return;
        }

        DependencyAnalyserLibImpl analyser = new DependencyAnalyserLibImpl(vertx);
        analyser.getProjectDependencies(folderPath).onComplete(result -> {
            if (result.succeeded()) {
                result.result().getPackageReports().forEach(packageReport -> {
                    String packageName = packageReport.getPackageName();

                    if (graph.getNode(packageName) == null) {
                        graph.addNode(packageName).setAttribute("ui.label", packageName);
                    }

                    packageReport.getClassReports().forEach(classReport -> {
                        String className = Paths.get(classReport.getSourceFileName()).getFileName().toString();
                        if (graph.getNode(className) == null) {
                            graph.addNode(className).setAttribute("ui.label", className);
                            graph.addEdge(packageName + "-" + className, packageName, className);
                        }

                        classReport.getDependencies().forEach(dep -> {
                            if (graph.getNode(dep) == null) {
                                graph.addNode(dep).setAttribute("ui.label", dep);
                            }

                            if (graph.getEdge(className + "-" + dep) == null) {
                                graph.addEdge(className + "-" + dep, className, dep);
                            }
                        });
                    });

                });

                JOptionPane.showMessageDialog(frame, "Analisi completata!");
            } else {
                JOptionPane.showMessageDialog(frame, "Errore durante l'analisi: " + result.cause().getMessage());
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DependencyAnalyser::new);
    }
}
