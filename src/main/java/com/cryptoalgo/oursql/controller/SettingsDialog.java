package com.cryptoalgo.oursql.controller;

import com.cryptoalgo.oursql.model.SettingsViewModel;
import com.cryptoalgo.oursql.support.I18N;
import com.cryptoalgo.oursql.support.ui.UIUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import java.io.IOException;

/**
 * Controller for the settings dialog
 */
public class SettingsDialog {
    @FXML // It is a good practice to use private fields and @FXML instead of public fields
    private ComboBox<String> langCombo;
    @FXML
    private CheckBox showSplashCheckbox;

    /**
     * Create a stage and show an add cluster dialog
     * @throws IOException If loading of the dialog FXML failed
     */
    public static void show() throws IOException {
        final var stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle(I18N.getString("app.title") + " " + I18N.getString("label.settings"));
        final var scene = new Scene(UIUtils.loadFXML("settings"));
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setMinWidth(400);
        stage.setMinHeight(200);
        stage.show();
    }

    @FXML
    private void initialize() {
        langCombo.getItems().addAll(I18N.getLocales());
        langCombo.valueProperty().bindBidirectional(SettingsViewModel.langProperty());
        langCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(String s) { return I18N.getHumanReadableNameFor(s); }
            @Override
            public String fromString(String s) { return I18N.getLocaleCodeFor(s); }
        });
        // Bindings really come in quite handy
        showSplashCheckbox.selectedProperty().bindBidirectional(SettingsViewModel.splashEnabledProperty());
    }

    @FXML
    private void handleClose(ActionEvent evt) {
        ((Stage) ((Button) evt.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void showAbout() {
        try {
            About.show();
        } catch (IOException ignored) {}
    }
}
