package org.example;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) {
        try {
            TypeFilter tf = new TypeFilter(args);
            tf.start();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}