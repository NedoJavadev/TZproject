package org.example;

import java.io.IOException;

public class Main {
    public static void main(String[] args){
        TypeFilter tf = new TypeFilter(args);
        try {
            tf.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}