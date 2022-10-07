package com.cryptoalgo.oursql.component;

import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A custom dialog with a {@link PasswordField}, to make up for the lack of a
 * <code>PasswordInputDialog</code> or similar in JavaFX. Of course, styles
 * are added to allow the dialog to fit in well with the application's theme.
 */
public class PasswordDialog extends Dialog<String> {
    private final PasswordField passwordField;

    /**
     * Create an instance of <code>PasswordDialog</code> with parameters
     * to populate the dialog.
     * @param title Title of dialog
     * @param header Header of dialog
     * @param body Body text, displayed right above the {@link PasswordField}
     * @param caption Caption text, displayed right below the {@link PasswordField}
     *                with a smaller font size. Can be <code>null</code> if no
     *                caption is required.
     */
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
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

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
            if (dialogButton == ButtonType.OK) return passwordField.getText();
            return null;
        });
    }
}
