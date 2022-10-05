package com.cryptoalgo.oursql;

import com.cryptoalgo.oursql.support.I18N;
import com.cryptoalgo.oursql.support.ui.Colors;
import com.cryptoalgo.oursql.support.ui.SpringInterpolator;
import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.Taskbar;

import java.io.IOException;

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

    private static final int SPLASH_WIDTH = 575;
    private static final int SPLASH_HEIGHT = 320;

    /**
     * Creates and animates the splash screen.
     * The JavaFX pure-code in here is a little too pure for my liking...
     * @return The stage of the splash screen
     */
    private Stage createSplash() {
        final var loadStage = new Stage();
        loadStage.initStyle(StageStyle.TRANSPARENT);
        loadStage.setWidth(SPLASH_WIDTH);
        loadStage.setHeight(SPLASH_HEIGHT);

        // Splashscreen nodes
        final var splashStack = new StackPane();
        final var splashContainer = new HBox(40);
        splashContainer.setAlignment(Pos.CENTER);
        final var icnStack = new StackPane();

        icnStack.setAlignment(Pos.CENTER);
        final var bgCircle = new Circle();
        bgCircle.setScaleX(0); bgCircle.setScaleY(0);
        bgCircle.setFill(Colors.SPLASH_BG);
        bgCircle.setRadius(Math.sqrt(Math.pow(SPLASH_WIDTH, 2) + Math.pow(SPLASH_HEIGHT, 2)));
        final var icn = new ImageView(UIUtils.getResourcePath("img/splash/bareIcon.png"));
        icn.setScaleX(0); icn.setScaleY(0);
        icn.setFitWidth(256);
        icn.setPreserveRatio(true);
        final var hammer = new ImageView(UIUtils.getResourcePath("img/splash/hammer.png"));
        hammer.setScaleX(0); hammer.setScaleY(0);
        hammer.setFitWidth(175);
        hammer.setPreserveRatio(true);
        hammer.setTranslateX(60);
        hammer.setTranslateY(60);
        icnStack.getChildren().addAll(icn, hammer);

        final Text
            title = new Text(I18N.getString("app.title")),
            desc = new Text(I18N.getString("app.desc")),
            author = new Text(I18N.getString("app.author"));
        title.setFont(Font.font(null, FontWeight.BLACK, 56));
        title.setStyle("-fx-fill: #fff");
        desc.setStyle("-fx-font-size: 1.5em; -fx-fill: #fff;");
        author.setStyle("-fx-font-size: 1.1em; -fx-fill: #FFFFFFC0;");
        title.setOpacity(0); desc.setOpacity(0); author.setOpacity(0);
        final var authorCont = new HBox(author); // Silly trick to right align one node
        authorCont.setAlignment(Pos.CENTER_RIGHT);
        final var spacer = new Pane();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        final var textContainer = new VBox(8, title, desc, spacer, authorCont);
        HBox.setMargin(textContainer, new Insets(60, 0, 60, 0));

        splashContainer.getChildren().addAll(icnStack, textContainer);
        splashStack.getChildren().addAll(bgCircle, splashContainer);
        UIUtils.clipToRadius(splashStack, 24);

        final var scene = new Scene(splashStack);
        scene.setFill(Color.TRANSPARENT);
        loadStage.setScene(scene);
        loadStage.show();

        // Icon transitions
        final var bgScale = new ScaleTransition(Duration.millis(500), bgCircle);
        bgScale.setToX(1); bgScale.setToY(1);
        bgScale.setInterpolator(new SpringInterpolator(210, 1, 20));
        final var s1 = new ScaleTransition(Duration.millis(550), icn);
        s1.setByX(1); s1.setByY(1);
        s1.setInterpolator(new SpringInterpolator(120, 1, 14));
        final var s2 = new ScaleTransition(Duration.millis(400), hammer);
        s2.setByX(1); s2.setByY(1);
        s2.setInterpolator(new SpringInterpolator(480, 0.6, 11));

        // Text transitions
        final var tTitle = new FadeTransition(Duration.millis(750), title);
        tTitle.setToValue(1);
        final var tDesc = new FadeTransition(Duration.millis(400), desc);
        tDesc.setToValue(1);
        tDesc.setDelay(Duration.millis(500));
        final var tAuthor = new FadeTransition(Duration.millis(400), author);
        tAuthor.setToValue(1);
        tAuthor.setDelay(Duration.millis(800));

        final var p = new ParallelTransition();
        p.getChildren().addAll(bgScale, s1);
        final var textFadeIn = new ParallelTransition();
        textFadeIn.getChildren().addAll(tTitle, tDesc, tAuthor);
        final var t = new SequentialTransition();
        t.getChildren().addAll(p, s2, textFadeIn);
        t.setDelay(Duration.millis(500));
        t.play();
        return loadStage;
    }

    private void setIcons(Stage stage) {
        // Set taskbar image for macOS platforms
        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            try {
                taskbar.setIconImage(
                    ImageIO.read(UIUtils.getResourceURL("img/appIcon.png"))
                );
            } catch (UnsupportedOperationException | IOException ignored) {}
        }
        stage.getIcons().add(new Image(UIUtils.getResourcePath("img/appIcon.png")));
    }

    @Override
    public void start(Stage stage) {
        setIcons(stage);

        // Show splash screen first
        Stage splashStage = createSplash();

        new Thread(() -> {
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            Scene scene;
            try { scene = new Scene(UIUtils.loadFXML("home")); } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            scene.setFill(Color.TRANSPARENT);
            stage.setTitle(I18N.getString("app.title"));

            // Fade out old stage
            final var fade = new FadeTransition(Duration.millis(400), splashStage.getScene().getRoot());
            fade.setToValue(0);
            fade.play();
            fade.setOnFinished(e -> Platform.runLater(() -> {
                stage.setScene(scene);
                stage.show();
                splashStage.close();
            }));
        }).start();
    }

    public static void main(String[] args) {
        launch();
    }
}