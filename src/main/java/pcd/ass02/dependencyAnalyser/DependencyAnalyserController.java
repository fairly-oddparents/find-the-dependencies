package pcd.ass02.dependencyAnalyser;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
            view.showError("Select a folder to analyze");
            return;
        }

        this.classCount = this.dependencyCount = 0;
        this.view.updateStats(this.classCount, this.dependencyCount);

        Observable.fromIterable(model.getJavaFiles(Paths.get(folder)))
                .subscribeOn(Schedulers.io())
                .map(model::parseClassDependencies)
                .observeOn(Schedulers.computation())
                .subscribe(
                        dep -> {
                            classCount++;
                            this.view.addToGraph(dep.getName(), dep.getDependencies());
                            view.updateStats(classCount, dependencyCount);
                        },
                        err -> view.showError("Error during analysis: " + err.getMessage())
                );
    }

}
