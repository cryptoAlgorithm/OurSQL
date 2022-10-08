package com.cryptoalgo.oursql.component;

import com.cryptoalgo.oursql.support.I18N;
import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Utility class to create help dialog stage/scene and populate them with content.
 */
public class HelpDialog {
    /**
     * Show the help dialog
     */
    public static void show() {
        final var helpStage = new Stage();
        helpStage.initStyle(StageStyle.UTILITY);
        helpStage.setTitle(I18N.getString("dialog.help.title"));
        helpStage.setMinWidth(550); helpStage.setMinHeight(200);
        helpStage.setWidth(600); helpStage.setHeight(600);
        final var wv = new WebView();
        wv.getEngine().load(UIUtils.getResourcePath("help/OurSQL.html"));
        wv.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.contains("OurSQL.html")) {
                wv.getEngine().load(UIUtils.getResourcePath("help/OurSQL.html"));
                new StyledAlert(
                    Alert.AlertType.WARNING,
                    "Navigation Blocked",
                    "You can't navigate out of this help page.",
                    "Please view the help docs online if you'd like to navigate to other pages."
                ).show();
            }
        });
        final var s = new Scene(wv);
        s.setFill(null); // Prevent flash of white before CSS loads

        helpStage.setScene(s);
        helpStage.show();
    }
}
