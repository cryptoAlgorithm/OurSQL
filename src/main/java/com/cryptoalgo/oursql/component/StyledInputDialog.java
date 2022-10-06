package com.cryptoalgo.oursql.component;

import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.scene.control.TextInputDialog;

/**
 * A styled {@link TextInputDialog} with styles that fit in with the
 * application's overall theme.
 */
public class StyledInputDialog extends TextInputDialog {
    /**
     * Convenience constructor to create an instance of <code>StyledInputDialog</code>.
     * @param title Title of dialog
     * @param header Header of dialog
     * @param body Content of dialog, displayed beside the text field
     */
    public StyledInputDialog(
        String title,
        String header,
        String body
    ) {
        super();
        setTitle(title);
        setHeaderText(header);
        setContentText(body);
        getDialogPane().getStylesheets().add(UIUtils.getResourcePath("css/dialog.css"));
    }
}
