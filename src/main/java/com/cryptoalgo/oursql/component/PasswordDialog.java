package com.cryptoalgo.oursql.component;

import com.cryptoalgo.oursql.support.I18N;
import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PasswordDialog extends Dialog<String> {
    private final PasswordField passwordField;

    public PasswordDialog(
        @NotNull String title,
        @NotNull String header,
        @Nullable String body,
        @Nullable String caption
    ) {
        super();
        getDialogPane().getStylesheets().add(UIUtils.getResourcePath("css/dialog.css"));
        getDialogPane().getScene().setFill(Color.TRANSPARENT); // Prevents flash of white when opening dialog
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
        b.getStyleClass().add("h5");
        b.setWrapText(true);
        c.getChildren().addAll(b, passwordField);
        c.getStyleClass().add("content");
        if (caption != null) c.getChildren().add(new Label(caption));

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
