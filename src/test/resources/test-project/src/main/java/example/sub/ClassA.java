package example.sub;

import example.utils.Helper;

public class ClassA {
    private ClassB b = new ClassB();

    public void doSomething() {
        b.compute();
        Helper.log("ClassA did something");
    }
}