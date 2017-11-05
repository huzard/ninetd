package com.nine.td.game.ui;

import com.google.common.base.Preconditions;
import com.nine.td.Game;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class Navigation implements NavigationDisplay {
    private Node root;

    private final ComboBox<String> mapList = new ComboBox<>();
    private final Button play = new Button();

    public Navigation() {}

    @Override
    public Node render() {
        if(this.root == null) {
            this.play.setGraphic(new FontIcon(FontAwesome.PLAY));
            this.play.setOnAction(event -> Game.getInstance().start());
            this.root = new ToolBar(this.mapList, this.play);
        }

        return this.root;
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
}
