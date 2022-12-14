package com.cryptoalgo.oursql.support.ui;

import com.cryptoalgo.oursql.OurSQL;
import com.cryptoalgo.oursql.model.SettingsViewModel;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Convenience methods for UI operations
 */
public class UIUtils {
    /**
     * Clips a provided pane to a specified corner radius.
     * For those pesky
     * @param targetPane Pane to clip
     * @param r Corner radius of the clipping rectangle
     */
    public static void clipToRadius(Region targetPane, int r) {
        Rectangle c = new Rectangle();
        c.widthProperty().bind(targetPane.widthProperty());
        c.heightProperty().bind(targetPane.heightProperty());
        c.setArcWidth(r);
        c.setArcHeight(r);
        targetPane.setClip(c);
    }

    /**
     * Load and return a requested FXML.
     * @param name Name of FXML to load, relative to <code>view/</code> (do not add file extension)
     * @return Loaded root pane from the requested FXML
     * @param <T> Type of root pane
     * @throws IOException If loading from the requested FXML file failed
     */
    public static <T extends Pane> T loadFXML(String name) throws IOException {
        return new FXMLLoader(
            OurSQL.class.getResource("view/" + name + ".fxml"),
            ResourceBundle.getBundle(
                "locales/strings",
                new Locale(SettingsViewModel.langProperty().get())
            )
        ).load();
    }

    /**
     * Create a stage for utility windows. Pre-styled with the model css.
     * @param title Title of stage
     * @param nodes Nodes to add to root {@link VBox}
     * @return Created stage ({@link Stage#show()} not called)
     */
    public static Stage createUtilityStage(String title, Node... nodes) {
        final var utilStage = new Stage();
        utilStage.initStyle(StageStyle.UTILITY);
        utilStage.setTitle(title);
        utilStage.setMinWidth(400); utilStage.setMaxWidth(400); utilStage.setWidth(400);
        utilStage.setHeight(100); utilStage.setMinHeight(70);
        final var parent = new VBox(nodes);
        parent.setPadding(new Insets(16));
        parent.getStylesheets().add(getResourcePath("css/model.css"));
        final var s = new Scene(parent);
        s.setFill(Color.TRANSPARENT);
        utilStage.setScene(s);
        return utilStage;
    }

    /**
     * Get non-null URL of a resource.
     * @param res Resource string
     * @return URL of requested resource
     */
    public static URL getResourceURL(String res) {
        return Objects.requireNonNull(OurSQL.class.getResource(res));
    }
    /**
     * Get non-null URL string of a resource. For those APIs that for some reason
     * have a parameter named "url" with the String type.
     * @param res Resource string
     * @return External string form of URL of requested resource
     */
    public static String getResourcePath(String res) {
        return getResourceURL(res).toExternalForm();
    }
}
