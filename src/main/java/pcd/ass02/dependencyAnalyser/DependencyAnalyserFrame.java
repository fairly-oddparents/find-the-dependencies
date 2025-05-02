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
    private final JButton selectFolderButton, analyzeButton;
    private final JLabel classesLabel, dependenciesLabel;
    private final Graph graph;

    public DependencyAnalyserFrame() {
        this.frame = new JFrame("Dependency Graph Analyzer");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(800, 600);
        this.frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        this.folderPathField = new JTextField(30);
        this.selectFolderButton = new JButton("Choose folder");
        this.analyzeButton = new JButton("Start analysis");
        topPanel.add(new JLabel("Source Root Folder:"));
        topPanel.add(this.folderPathField);
        topPanel.add(this.selectFolderButton);
        topPanel.add(this.analyzeButton);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        this.classesLabel = new JLabel();
        this.dependenciesLabel = new JLabel();
        this.updateStats(0, 0);

        statsPanel.add(this.classesLabel, BorderLayout.CENTER);
        statsPanel.add(Box.createHorizontalStrut(20));
        statsPanel.add(this.dependenciesLabel, BorderLayout.CENTER);

        System.setProperty("org.graphstream.ui", "swing");
        graph = new MultiGraph("Dipendenze");
        graph.setAttribute("ui.stylesheet", "node { text-size: 14px; text-color: black; " +
                        "text-background-mode: plain; text-background-color: white; " +
                        "text-padding: 5px, 3px; text-offset: 10px, 10px; }"
        );

        SwingViewer viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        ViewPanel viewPanel = (ViewPanel) viewer.addDefaultView(false);


        this.frame.add(topPanel, BorderLayout.NORTH);
        this.frame.add(viewPanel, BorderLayout.CENTER);
        this.frame.add(statsPanel, BorderLayout.SOUTH);
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);
    }

    public JFrame getFrame(){
        return this.frame;
    }
    public JTextField getFolderPathField(){
        return this.folderPathField;
    }
    public JButton getSelectFolderButton(){
        return this.selectFolderButton;
    }
    public JButton getAnalyzeButton(){
        return this.analyzeButton;
    }
    public Graph getGraph(){
        return this.graph;
    }

    public void setFolderPath(String path) {
        this.folderPathField.setText(path);
    }

    public void updateStats(int classCount, int dependencyCount) {
        this.classesLabel.setText("Classes: " + classCount);
        this.dependenciesLabel.setText("Dependencies: " + dependencyCount);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error: ", JOptionPane.ERROR_MESSAGE);
    }

}
