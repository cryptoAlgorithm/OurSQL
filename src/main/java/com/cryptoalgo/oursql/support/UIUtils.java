package com.cryptoalgo.oursql.support;

import com.cryptoalgo.oursql.OurSQL;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.Locale;
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

    public static <T extends Pane> T loadFXML(String name) throws IOException {
        return new FXMLLoader(
            OurSQL.class.getResource("view/" + name + ".fxml"),
            ResourceBundle.getBundle("locales/strings", new Locale("en"))
        ).load();
    }
}
