package pcd.ass02.dependencyAnalyser;

import javax.swing.*;
import java.awt.*;
import java.util.Set;


public class DependencyAnalyserFrame{

    private final JFrame frame;
    private final JTextField folderPathField;
    private final JButton selectFolderButton, analyzeButton;
    private final JLabel classesLabel, dependenciesLabel;
    private final GraphPanel graphPanel;

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

        this.graphPanel = new GraphPanel();

        this.frame.add(topPanel, BorderLayout.NORTH);
        this.frame.add(this.graphPanel, BorderLayout.CENTER);
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
    public JButton getAnalyzeButton() {
        return this.analyzeButton;
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

    public void addToGraph(String name, Set<String> dependencies) {
        this.graphPanel.add(name, dependencies);
    }
}
