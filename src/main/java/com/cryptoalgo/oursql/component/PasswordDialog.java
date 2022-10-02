package com.cryptoalgo.oursql.component;

import com.cryptoalgo.oursql.OurSQL;
import com.cryptoalgo.oursql.support.I18N;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class PasswordDialog extends Dialog<String> {
    private final PasswordField passwordField;

    public PasswordDialog(
        String title,
        String header,
        String body
    ) {
        super();
        getDialogPane().getStylesheets().add(
            Objects.requireNonNull(
                OurSQL.class.getResource("css/dialog.css")
            ).toExternalForm()
        );
        setTitle(title);
        setHeaderText(header);

        // Add action buttons
        ButtonType passwordButtonType = new ButtonType(
            I18N.getString("action.ok"),
            ButtonBar.ButtonData.OK_DONE
        );
        getDialogPane().getButtonTypes().addAll(passwordButtonType, ButtonType.CANCEL);

        // Populate content
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        VBox c = new VBox(8);
        Label b = new Label(body);
        b.setWrapText(true);
        c.getChildren().add(b);
        c.getChildren().add(passwordField);
        c.getStyleClass().add("content");
        getDialogPane().setContent(c);
        Platform.runLater(passwordField::requestFocus);

        setResultConverter(dialogButton -> {
            if (dialogButton == passwordButtonType) {
                return passwordField.getText();
            }
            return null;
        });
    }
}
