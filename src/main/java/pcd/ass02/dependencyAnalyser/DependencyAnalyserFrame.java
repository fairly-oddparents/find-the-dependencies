package pcd.ass02.dependencyAnalyser;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;

import javax.swing.*;
import java.awt.*;

import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;


public class DependencyAnalyserFrame{

    private final JFrame frame;
    private final JTextField folderPathField;
    private final JButton selectFolderButton;
    private final JButton analyzeButton;
    private final JLabel classesLabel;
    private final JLabel dependenciesLabel;
    private final Graph graph;

    public DependencyAnalyserFrame() {
        frame = new JFrame("Dependency Graph Analyzer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        folderPathField = new JTextField(30);
        selectFolderButton = new JButton("Scegli cartella…");
        analyzeButton = new JButton("Avvia analisi");
        topPanel.add(new JLabel("Source Root Folder:"));
        topPanel.add(folderPathField);
        topPanel.add(selectFolderButton);
        topPanel.add(analyzeButton);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        classesLabel = new JLabel("Classi: 0");
        dependenciesLabel = new JLabel("Dipendenze: 0");
        statsPanel.add(classesLabel, BorderLayout.CENTER);
        statsPanel.add(Box.createHorizontalStrut(20));
        statsPanel.add(dependenciesLabel, BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(statsPanel, BorderLayout.SOUTH);

        System.setProperty("org.graphstream.ui", "swing");
        graph = new MultiGraph("Dipendenze");
        graph.setAttribute("ui.stylesheet", "node { text-size: 14px; text-color: black; " +
                        "text-background-mode: plain; text-background-color: white; " +
                        "text-padding: 5px, 3px; text-offset: 10px, 10px; }"
        );

        SwingViewer viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        ViewPanel viewPanel = (ViewPanel) viewer.addDefaultView(false);

        frame.add(viewPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public JFrame getFrame(){
        return frame;
    }
    public JTextField getFolderPathField(){
        return folderPathField;
    }
    public JButton getSelectFolderButton(){
        return selectFolderButton;
    }
    public JButton getAnalyzeButton(){
        return analyzeButton;
    }
    public Graph getGraph(){
        return graph;
    }

    public void setFolderPath(String path) {
        folderPathField.setText(path);
    }

    public void updateStats(int classCount, int dependencyCount) {
        classesLabel.setText("Classi: " + classCount);
        dependenciesLabel.setText("Dipendenze: " + dependencyCount);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Errore", JOptionPane.ERROR_MESSAGE);
    }

}
