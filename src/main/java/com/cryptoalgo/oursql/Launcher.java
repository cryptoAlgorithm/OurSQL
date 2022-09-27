package com.cryptoalgo.oursql;

/**
 * Application entrypoint when packaged into a JAR.
 * Why is this even necessary? Did JavaFX devs not consider
 * the need to package a JavaFX application into a JAR for
 * distribution?
 */
public class Launcher {
    public static void main(String[] args) {
        OurSQL.main(args);
    }
}
