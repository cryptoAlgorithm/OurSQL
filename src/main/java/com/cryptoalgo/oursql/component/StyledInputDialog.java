package com.cryptoalgo.oursql.component;

import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.scene.control.TextInputDialog;

public class StyledInputDialog extends TextInputDialog {
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
