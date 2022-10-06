package com.cryptoalgo.oursql.support;

import javafx.application.Platform;

import java.util.concurrent.Semaphore;

/**
 * Small utility class to make working with threads a little easier.
 */
public class AsyncUtils {
    /**
     * Run the runnable on the UI thread and wait for it to complete (blocking).
     * @param r Runnable to run on UI thread
     */
    public static void runLaterAndWait(Runnable r) {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(() -> {
            r.run();
            semaphore.release();
        });
        try { semaphore.acquire(); } catch (InterruptedException ignored) {}
    }
}
