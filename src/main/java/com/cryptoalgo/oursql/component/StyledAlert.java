package com.cryptoalgo.oursql.component;

import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.scene.control.Alert;

public class StyledAlert extends Alert {
    public StyledAlert(
        AlertType type,
        String title,
        String header,
        String body
    ) {
        super(type);
        getDialogPane().getStylesheets().add(UIUtils.getResourcePath("css/dialog.css"));
        setTitle(title);
        setHeaderText(header);
        setContentText(body);
    }
}
