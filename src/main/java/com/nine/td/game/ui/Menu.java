package com.nine.td.game.ui;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.nine.td.CssStyle;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.stream.Collectors;

public class Menu implements MenuDisplay {
    private Node root;

    private final ComboBox<String> mapList = new ComboBox<>();
    private final ComboBox<String> cssList = new ComboBox<>();
    private final Button play = new Button();

    public Menu() {}

    @Override
    public Node render() {
        if(this.root == null) {
            this.root = new ToolBar(this.mapList, this.cssList, this.play);
        }

        return this.root;
    }

    @Override
    public void setStyles(List<String> styleList) {
        Preconditions.checkArgument(styleList != null, "null css style list");
        Preconditions.checkArgument(!styleList.isEmpty(), "css style list empty");

        this.cssList.getItems().setAll(styleList);
        this.cssList.setValue(this.cssList.getItems().get(0));
    }

    @Override
    public void setMaps(List<String> mapList) {
        Preconditions.checkArgument(mapList != null, "null map list");
        Preconditions.checkArgument(!mapList.isEmpty(), "map list empty");

        this.mapList.getItems().setAll(mapList);
        this.mapList.setValue(this.mapList.getItems().get(0));
    }

    @Override
    public void onMapSelected(ChangeListener<String> mapSelectedHandler) {
        this.mapList.valueProperty().addListener(mapSelectedHandler);
    }

    @Override
    public void onGameStart(EventHandler<ActionEvent> handler) {
        this.play.setOnAction(handler);
    }

    @Override
    public void onThemeSwitch(ChangeListener<String> styleChangedHandler) {
        this.cssList.valueProperty().addListener(styleChangedHandler);
    }
}
