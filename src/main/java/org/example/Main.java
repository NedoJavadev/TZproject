package org.example;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) {
        TypeFilter tf = new TypeFilter(args);
        try {
            tf.start();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}