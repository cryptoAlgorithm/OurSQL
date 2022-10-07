package com.cryptoalgo.oursql.controller;

import com.cryptoalgo.oursql.support.I18N;
import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URL;

/**
 * This class simply provides a method to show the About dialog, and
 * handle the one button in the about page
 */
public class About {
    /**
     * Show the About dialog
     */
    public static void show() throws IOException {
        final var root = UIUtils.loadFXML("about");
        final var stage = new Stage();
        stage.setWidth(300);
        stage.setResizable(false);
        stage.setTitle(I18N.getString("app.title") + " " + I18N.getString("label.about"));
        final var s = new Scene(root);
        s.setFill(Color.TRANSPARENT); // Prevent flash of white before CSS loads
        stage.setScene(s);
        stage.show();
    }

    @FXML
    private void handleSwiftcordCTA() {
        if (Desktop.isDesktopSupported()) try {
            Desktop.getDesktop().browse(new URL("https://github.com/SwiftcordApp/Swiftcord").toURI());
        } catch (Exception ignored) {}
    }
}
