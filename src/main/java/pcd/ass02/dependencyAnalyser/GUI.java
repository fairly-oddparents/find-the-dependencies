package pcd.ass02.dependencyAnalyser;

import javax.swing.*;
import java.awt.*;
import java.util.Set;


public class GUI {

    public static final int GAP = 10;
    private final JFrame frame;
    private final JLabel classesLabel, dependenciesLabel;
    private final GraphPanel graphPanel;
    private Controller controller;

    public GUI() {
        this.frame = new JFrame("Dependency Graph Analyzer");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(800, 600);
        this.frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, GAP, GAP));
        JTextField folderPathField = new JTextField(30);
        JButton selectFolderButton = new JButton("Choose folder");
        selectFolderButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                folderPathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        JButton analyzeButton = new JButton("Start analysis");
        analyzeButton.addActionListener(e -> {
            if (controller != null) {
                controller.startAnalysis(folderPathField.getText());
            } else {
                showError("Controller not correctly set");
            }
        });
        topPanel.add(new JLabel("Source Root Folder:"));
        topPanel.add(folderPathField);
        topPanel.add(selectFolderButton);
        topPanel.add(analyzeButton);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, GAP, GAP));
        this.classesLabel = new JLabel();
        this.dependenciesLabel = new JLabel();
        this.updateStats(0, 0);
        statsPanel.add(this.classesLabel);
        statsPanel.add(this.dependenciesLabel);

        this.graphPanel = new GraphPanel();

        this.frame.add(topPanel, BorderLayout.NORTH);
        this.frame.add(this.graphPanel, BorderLayout.CENTER);
        this.frame.add(statsPanel, BorderLayout.SOUTH);
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);
    }

    public void setController(Controller controller) {
        this.controller = controller;
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

    public void clearGraph(){
        this.graphPanel.clear();
    }
}
