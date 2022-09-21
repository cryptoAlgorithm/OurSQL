package com.cryptoalgo.oursql.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;

public class Home {
    @FXML
    private Accordion categoryList;

    @FXML
    private void addCluster() {

    }

    @FXML
    private void initialize() {
        for (int i = 0; i < 10; i++) {
            TitledPane d = new TitledPane();
            d.setText("Category " + i);
            d.setContent(new Label("Content " + i));
            categoryList.getPanes().add(d);
        }
    }
}