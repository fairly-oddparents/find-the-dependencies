package pcd.ass02.reactive;

public class Launcher {
    public static void main(String[] args) {
        GUI view = new GUI();
        DependencyAnalyser model = new DependencyAnalyser();
        Controller controller = new Controller(view, model);
        view.setController(controller);
    }
}
