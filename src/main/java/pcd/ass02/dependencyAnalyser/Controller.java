package pcd.ass02.dependencyAnalyser;

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.nio.file.Paths;

public class Controller {

    private final GUI view;
    private final DependencyAnalyser model;

    private int classCount = 0;
    private int dependencyCount = 0;

    private static final int BUFFER_SIZE = 1000;

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
        this.view.clearGraph();
        this.classCount = this.dependencyCount = 0;
        this.view.updateStats(this.classCount, this.dependencyCount);

        Flowable.fromIterable(model.getJavaFiles(Paths.get(path)))
                .onBackpressureBuffer(BUFFER_SIZE, () -> {}, BackpressureOverflowStrategy.ERROR)
                .subscribeOn(Schedulers.io()) //background elastic thread pool for slow blocking operations (Files.walk())
                .map(model::parseClassDependencies)
                .observeOn(Schedulers.single()) //single thread for subscribers (for sequential work and UI)
                .subscribe(
                        dep -> {
                            classCount++;
                            this.view.addToGraph(dep.getName(), dep.getDependencies());
                            view.updateStats(classCount, dependencyCount);
                        },
                        err -> {
                            if (err instanceof MissingBackpressureException) {
                                view.showError("Too many classes, full buffer");
                            } else {
                                view.showError(err.getMessage());
                            }
                        }
                );
    }

}
