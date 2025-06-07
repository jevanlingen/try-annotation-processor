package org.jdriven;

import java.util.List;

public class ExampleCodeSmell {
    public static void main(String[] args) {
        // Enable for compile error:
        //List.of("first").get(0);
    }
}
