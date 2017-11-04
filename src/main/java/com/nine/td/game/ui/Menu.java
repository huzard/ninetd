package com.nine.td.game.ui;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

public class Menu implements MenuDisplay {
    private Group root;

    private final ComboBox<String> mapList = new ComboBox<>();
    private final ComboBox<String> cssList = new ComboBox<>();
    private final Button play = new Button();

    public Menu() {}

    @Override
    public Group render() {
        if(this.root == null) {
            
            HBox selectors = new HBox(this.mapList, this.cssList, this.play);
            selectors.setSpacing(5.0);
            this.root = new Group(selectors);
        }

        return this.root;
    }

    @Override
    public void addStyle(String styleName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(styleName), "css styleName error");

        this.cssList.getItems().add(styleName);

        if(this.cssList.getItems().size() == 1) {
            this.cssList.setValue(styleName);
        }
    }

    @Override
    public void addMap(String mapName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(mapName), "css styleName error");

        this.mapList.getItems().add(mapName);

        if(this.mapList.getItems().size() == 1) {
            this.mapList.setValue(mapName);
        }
    }

    @Override
    public void onMapSelected(ChangeListener<String> mapSelectedHandler) {
        this.mapList.valueProperty().addListener(mapSelectedHandler);
    }

    @Override
    public void onGameStart(EventHandler<ActionEvent> handler) {
        this.play.setOnAction(handler);
    }

    public String getSelectedMapName() {
        return this.mapList.getValue();
    }
}
