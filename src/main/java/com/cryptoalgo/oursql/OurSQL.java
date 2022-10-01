package com.cryptoalgo.oursql;

import com.cryptoalgo.oursql.support.I18N;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
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
        Font.loadFont(OurSQL.class.getResourceAsStream("fonts/SpaceMono-Regular.ttf"), 10);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
            OurSQL.class.getResource("view/home.fxml"),
            ResourceBundle.getBundle("locales/strings", new Locale("en"))
        );
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle(I18N.getString("app.title"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}