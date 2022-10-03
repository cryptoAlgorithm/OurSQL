package com.cryptoalgo.oursql.support;

import javafx.application.Platform;

import java.util.concurrent.Semaphore;

public class AsyncUtils {
    public static void runLaterAndWait(Runnable r) {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(() -> {
            r.run();
            semaphore.release();
        });
        try { semaphore.acquire(); } catch (InterruptedException ignored) {}
    }
}
