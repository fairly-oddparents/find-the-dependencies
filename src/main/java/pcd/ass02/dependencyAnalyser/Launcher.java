package pcd.ass02.dependencyAnalyser;

public class Launcher {
    public static void main(String[] args) {
        DependencyAnalyserFrame view = new DependencyAnalyserFrame();
        DependencyAnalyser model = new DependencyAnalyser();
        new DependencyAnalyserController(view, model);
    }
}
