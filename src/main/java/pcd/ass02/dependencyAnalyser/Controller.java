package pcd.ass02.dependencyAnalyser;

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    private final GUI view;
    private final DependencyAnalyser model;

    private int classCount = 0;
    private int dependencyCount = 0;

    private static final int BUFFER_SIZE = 1000;

    private List<Disposable> disposables;

    public Controller(GUI view, DependencyAnalyser model) {
        this.view  = view;
        this.model = model;
        this.disposables = new ArrayList<>();
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

        this.disposables.add(Flowable.fromIterable(model.getJavaFiles(Paths.get(path)))
                .onBackpressureBuffer(BUFFER_SIZE, () -> {}, BackpressureOverflowStrategy.ERROR)
                .subscribeOn(Schedulers.io()) //background elastic thread pool for slow blocking operations (Files.walk())
                .map(model::parseClassDependencies)
                .observeOn(Schedulers.single()) //single thread for subscribers (for sequential work and UI)
                .subscribe(
                        dep -> {
                            classCount++;
                            dependencyCount += dep.getDependencies().size();
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
                )
        );
    }

    public void onDestroy() {
        this.disposables.forEach(Disposable::dispose);
        this.disposables.clear();
    }

}
