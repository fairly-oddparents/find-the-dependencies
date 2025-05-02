package pcd.ass02.dependencyAnalyser;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.nio.file.Paths;

public class Controller {

    private final GUI view;
    private final DependencyAnalyser model;

    private int classCount = 0;
    private int dependencyCount = 0;

    public Controller(GUI view, DependencyAnalyser model) {
        this.view  = view;
        this.model = model;
    }

    public void startAnalysis(String folderPath) {
        String path = folderPath.trim();
        if (path.isEmpty()) {
            view.showError("Select a folder to analyze");
            return;
        }

        this.classCount = this.dependencyCount = 0;
        this.view.updateStats(this.classCount, this.dependencyCount);

        Observable.fromIterable(model.getJavaFiles(Paths.get(path)))
                .subscribeOn(Schedulers.io())
                .map(model::parseClassDependencies)
                .observeOn(Schedulers.computation())
                .subscribe(
                        dep -> {
                            classCount++;
                            this.view.addToGraph(dep.getName(), dep.getDependencies());
                            view.updateStats(classCount, dependencyCount);
                        },
                        err -> view.showError(err.getMessage())
                );
    }

}
