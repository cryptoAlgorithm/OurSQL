package com.cryptoalgo.oursql.controller;

import com.cryptoalgo.oursql.OurSQL;
import com.cryptoalgo.oursql.support.I18N;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class AddClusterDialog {
    public static void show() throws IOException {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle(I18N.getString("action.addCluster"));
        stage.setScene(new Scene(FXMLLoader.load(
            Objects.requireNonNull(OurSQL.class.getResource("view/add-cluster.fxml"))
        )));
        stage.show();
    }
}
