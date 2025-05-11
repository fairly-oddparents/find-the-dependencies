package pcd.ass02.reactive;

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    private static final int BUFFER_SIZE = 1000;

    private final GUI view;
    private final DependencyAnalyser model;
    private final List<Disposable> disposables;

    private int classCount = 0;
    private int dependencyCount = 0;

    public Controller(GUI view, DependencyAnalyser model) {
        this.view  = view;
        this.model = model;
        this.disposables = new ArrayList<>();
    }

    public void startAnalysis(String folderPath) {
        String path = folderPath.trim();
        if (path.isEmpty()) {
            view.showError("Before starting the analysis, select a folder to inspect.");
            return;
        }
        this.view.clearGraph();
        this.classCount = this.dependencyCount = 0;
        this.view.updateStats(this.classCount, this.dependencyCount);

        this.disposables.add(Flowable.fromIterable(model.getJavaFiles(Paths.get(path)))
                .onBackpressureBuffer(BUFFER_SIZE, () -> {}, BackpressureOverflowStrategy.ERROR)
                .subscribeOn(Schedulers.io())
                .map(model::parseClassDependencies)
                .observeOn(Schedulers.single())
                .subscribe(
                        dep -> {
                            classCount++;
                            dependencyCount += dep.getDependencies().size();
                            this.view.addToGraph(dep.getName(), dep.getDependencies());
                            view.updateStats(classCount, dependencyCount);
                        },
                        err -> view.showError(err instanceof MissingBackpressureException
                                ? "Too many files to process at once, given the buffer limit set. Consider a smaller folder."
                                : err.getMessage()
                        )
                )
        );
    }

    public void onDestroy() {
        this.disposables.forEach(Disposable::dispose);
        this.disposables.clear();
    }

}
