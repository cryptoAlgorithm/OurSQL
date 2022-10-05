package com.cryptoalgo.oursql;

import com.cryptoalgo.oursql.support.I18N;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Taskbar;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Main application entrypoint
 * <br>
 * Don't use the SceneBuilder scum on this project. It creates completely
 * horrible looking fxml files that are impossible to read. All the fxml
 * files contained herein were created and edited fully by hand.
 */
public class OurSQL extends Application {
    @Override
    public void init() {
        Font.loadFont(OurSQL.class.getResourceAsStream("fonts/IBMPlexMono.ttf"), 12);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Set taskbar image for macOS platforms
        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            try {
                taskbar.setIconImage(
                    ImageIO.read(Objects.requireNonNull(OurSQL.class.getResource("img/appIcon.png")))
                );
            } catch (UnsupportedOperationException ignored) {}
        }
        stage.getIcons().add(new Image(
            Objects.requireNonNull(OurSQL.class.getResource("img/appIcon.png")).toExternalForm()
        ));
        new Thread(() -> {
            FXMLLoader fxmlLoader = new FXMLLoader(
                OurSQL.class.getResource("view/home.fxml"),
                ResourceBundle.getBundle("locales/strings", new Locale("en"))
            );
            Scene scene;
            try { scene = new Scene(fxmlLoader.load()); } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            stage.setTitle(I18N.getString("app.title"));
            Platform.runLater(() -> {
                stage.setScene(scene);
                stage.show();
            });
        }).start();
    }

    public static void main(String[] args) {
        launch();
    }
}