package com.cryptoalgo.oursql;

/**
 * Application entrypoint when packaged into a JAR.
 * Why is this even necessary? Did JavaFX devs not consider
 * the need to package a JavaFX application into a JAR for
 * distribution?
 */
public class Launcher {
    /**
     * Launcher entrypoint, calls {@link OurSQL#main(String[] args)}
     * @param args Commandline args
     */
    public static void main(String[] args) {
        OurSQL.main(args);
    }
}
