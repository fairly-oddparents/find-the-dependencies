package pcd.ass02.dependencyAnalyser;

public class Launcher {
    public static void main(String[] args) {
        GUI view = new GUI();
        DependencyAnalyser model = new DependencyAnalyser();
        new Controller(view, model);
    }
}
