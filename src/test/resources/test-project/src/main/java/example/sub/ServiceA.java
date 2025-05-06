package example.sub;

public class ServiceA {
    private final ClassA a = new ClassA();
    private final ClassB b = new ClassB();

    public void serve() {
        a.doSomething();
        b.compute();
    }
}