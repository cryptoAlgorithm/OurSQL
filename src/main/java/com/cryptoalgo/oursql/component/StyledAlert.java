package com.cryptoalgo.oursql.component;

import com.cryptoalgo.oursql.OurSQL;
import javafx.scene.control.Alert;

import java.util.Objects;

public class StyledAlert extends Alert {
    public StyledAlert(
        AlertType type,
        String title,
        String header,
        String body
    ) {
        super(type);
        getDialogPane().getStylesheets().add(
            Objects.requireNonNull(
                OurSQL.class.getResource("css/dialog.css")
            ).toExternalForm()
        );
        setTitle(title);
        setHeaderText(header);
        setContentText(body);
    }
}
