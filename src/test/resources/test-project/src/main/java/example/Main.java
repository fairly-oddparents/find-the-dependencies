package example;

import example.sub.ServiceA;

public class Main {
    public static void main(String[] args) {
        ServiceA service = new ServiceA();
        service.serve();
    }
}