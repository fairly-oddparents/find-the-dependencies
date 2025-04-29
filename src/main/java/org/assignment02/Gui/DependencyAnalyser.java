package org.assignment02.Gui;

import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DependencyAnalyser {

    private final JFrame frame;
    private final JTextField folderPathField;
    private final JLabel classesLabel, dependenciesLabel;
    private final Graph graph;

    private int classCount = 0;
    private int dependencyCount = 0;

    public DependencyAnalyser() {
        frame = new JFrame("Dependency Graph Analyzer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        folderPathField = new JTextField(30);
        JButton selectFolderButton = new JButton("Scegli cartella...");
        JButton analyzeButton = new JButton("Avvia analisi");

        topPanel.add(new JLabel("Source Root Folder:"));
        topPanel.add(folderPathField);
        topPanel.add(selectFolderButton);
        topPanel.add(analyzeButton);

        JPanel statsPanel = new JPanel();
        classesLabel = new JLabel("Classi: 0");
        dependenciesLabel = new JLabel("Dipendenze: 0");
        statsPanel.add(classesLabel);
        statsPanel.add(dependenciesLabel);

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
        graph.display();

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(statsPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

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

        graph.clear();
        classCount = 0;
        dependencyCount = 0;

        Observable.fromIterable(getJavaFiles(Paths.get(folderPath)))
                .subscribeOn(Schedulers.io())
                .map(this::parseClassDependencies)
                .observeOn(Schedulers.computation())
                .subscribe(dep -> {
                    classCount++;
                    addToGraph(dep);
                    updateStats();
                }, err -> JOptionPane.showMessageDialog(frame, "Errore: " + err.getMessage()), () -> JOptionPane.showMessageDialog(frame, "Analisi completata!"));
    }

    private void updateStats() {
        classesLabel.setText("Classi: " + classCount);
        dependenciesLabel.setText("Dipendenze: " + dependencyCount);
    }

    private static class ClassDependency {
        final String className;
        final Set<String> dependencies;

        ClassDependency(String className, Set<String> dependencies) {
            this.className = className;
            this.dependencies = dependencies;
        }
    }

    private ClassDependency parseClassDependencies(Path javaFile) {
        try {
            JavaParser parser = new JavaParser();
            CompilationUnit cu = parser.parse(javaFile).getResult().orElseThrow();
            String className = cu.getPrimaryTypeName().orElse(javaFile.getFileName().toString());
            Set<String> deps = cu.findAll(ClassOrInterfaceType.class)
                    .stream()
                    .map(NodeWithSimpleName::getNameAsString)
                    .collect(Collectors.toSet());
            return new ClassDependency(className, deps);
        } catch (IOException e) {
            return new ClassDependency("Unknown", Set.of());
        }
    }

    private void addToGraph(ClassDependency dep) {
        if (graph.getNode(dep.className) == null) {
            graph.addNode(dep.className).setAttribute("ui.label", dep.className);
        }
        for (String d : dep.dependencies) {
            if (graph.getNode(d) == null) {
                graph.addNode(d).setAttribute("ui.label", d);
            }
            String edgeId = dep.className + "-" + d;
            if (graph.getEdge(edgeId) == null) {
                graph.addEdge(edgeId, dep.className, d);
                dependencyCount++;
            }
        }
    }

    private Set<Path> getJavaFiles(Path root) {
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(f -> f.toString().endsWith(".java"))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            return Set.of();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DependencyAnalyser::new);
    }
}
