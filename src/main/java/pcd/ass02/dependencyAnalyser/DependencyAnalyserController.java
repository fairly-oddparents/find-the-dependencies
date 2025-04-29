package pcd.ass02.dependencyAnalyser;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.graphstream.graph.Graph;

import javax.swing.*;
import java.nio.file.Paths;

public class DependencyAnalyserController {

    private final DependencyAnalyserFrame view;
    private final DependencyAnalyser model;

    private int classCount = 0;
    private int dependencyCount = 0;

    public DependencyAnalyserController(DependencyAnalyserFrame view, DependencyAnalyser model) {
        this.view  = view;
        this.model = model;
        initView();
        initController();
    }

    private void initView() {
        view.getFrame().setLocationRelativeTo(null);
        view.getFrame().setVisible(true);
    }

    private void initController() {
        view.getSelectFolderButton().addActionListener(e -> chooseFolder());
        view.getAnalyzeButton().addActionListener(e -> startAnalysis());
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(view.getFrame()) == JFileChooser.APPROVE_OPTION) {
            view.setFolderPath(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void startAnalysis() {
        String folder = view.getFolderPathField().getText().trim();
        if (folder.isEmpty()) {
            view.showError("Seleziona una cartella prima di avviare l'analisi!");
            return;
        }

        classCount = 0;
        dependencyCount = 0;
        Graph graph = view.getGraph();
        graph.clear();
        view.updateStats(classCount, dependencyCount);

        Observable.fromIterable(model.getJavaFiles(Paths.get(folder)))
                .subscribeOn(Schedulers.io())
                .map(model::parseClassDependencies)
                .observeOn(Schedulers.computation())
                .subscribe(
                        dep -> {
                            classCount++;
                            addToGraph(graph, dep);
                            view.updateStats(classCount, dependencyCount);
                        },
                        err -> view.showError("Errore durante l'analisi: " + err.getMessage()),
                        view::showCompletion
                );
    }

    private void addToGraph(Graph graph, ClassDependency dep) {
        if (graph.getNode(dep.getName()) == null) {
            graph.addNode(dep.getName())
                    .setAttribute("ui.label", dep.getName());
        }
        for (String d : dep.getDependencies()) {
            if (graph.getNode(d) == null) {
                graph.addNode(d).setAttribute("ui.label", d);
            }
            String edgeId = dep.getName() + "→" + d;
            if (graph.getEdge(edgeId) == null) {
                graph.addEdge(edgeId, dep.getName(), d, true);
                dependencyCount++;
            }
        }
    }
}
