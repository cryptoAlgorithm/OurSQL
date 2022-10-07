package com.cryptoalgo.oursql.component;

import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;

/**
 * A styled ${@link Alert} dialog with styles to better fit in with
 * the application's overall theme.
 */
public class StyledAlert extends Alert {
    /**
     * Convenience constructor to create an instance of <code>StyledAlert</code>.
     * @param type Type of alert - Affects icon and buttons available
     * @param title Title of alert dialog
     * @param header Header of alert
     * @param body Content text in alert
     */
    public StyledAlert(
        AlertType type,
        String title,
        String header,
        String body
    ) {
        super(type);
        getDialogPane().getStylesheets().add(UIUtils.getResourcePath("css/dialog.css"));
        getDialogPane().getScene().setFill(Color.TRANSPARENT); // Prevents flash of white when opening dialog
        setTitle(title);
        setHeaderText(header);
        setContentText(body);
    }
}
