package com.nine.td.game.ui;

import com.google.common.base.Preconditions;
import com.nine.td.Game;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.stream.Stream;

public class Navigation implements NavigationDisplay {
    private Node root;

    private final ComboBox<String> mapList = new ComboBox<>();

    //Controls
    private final Button play = new Button("", new FontIcon(FontAwesome.PLAY));
    private final Button pause = new Button("", new FontIcon(FontAwesome.PAUSE));
    private final Button stop = new Button("", new FontIcon(FontAwesome.STOP));

    //Speed
    private final Button accelerate = new Button("", new FontIcon(FontAwesome.ANGLE_DOUBLE_RIGHT));
    private final Button normal = new Button("", new FontIcon(FontAwesome.ANGLE_RIGHT));
    private final Button decelerate = new Button("", new FontIcon(FontAwesome.ANGLE_DOUBLE_LEFT));

    public Navigation() {}

    @Override
    public Node render() {
        if(this.root == null) {
            this.play.setOnAction(event -> Game.getInstance().start());
            this.pause.setOnAction(event -> Game.getInstance().pause());
            this.stop.setOnAction(event -> Game.getInstance().stop());
            this.accelerate.setOnAction(event -> Game.getInstance().changeSpeed(2));
            this.normal.setOnAction(event -> Game.getInstance().changeSpeed(1.0));
            this.decelerate.setOnAction(event -> Game.getInstance().changeSpeed(0.5));

            Game.getInstance().onGameStarted(() -> disableNodes(this.play, this.mapList));
            Game.getInstance().onGamePaused(()  -> disableNodes(this.pause, this.mapList, this.decelerate, this.accelerate, this.normal));
            Game.getInstance().onGameStopped(() -> disableNodes(this.pause, this.stop, this.decelerate, this.accelerate, this.normal));

            this.disableNodes(this.pause, this.stop, this.decelerate, this.accelerate, this.normal);

            Pane separation = new Pane();
            HBox.setHgrow(separation, Priority.ALWAYS);

            this.root = new ToolBar(
                    this.mapList,
                    this.play,
                    this.pause,
                    this.stop,
                    separation,
                    this.decelerate,
                    this.normal,
                    this.accelerate
            );
        }

        return this.root;
    }

    private void disableNodes(Node ... nodes) {
        Stream.of(this.play, this.pause, this.stop, this.mapList, this.decelerate, this.accelerate, this.normal).forEach(node -> node.setDisable(false));
        Stream.of(nodes).forEach(button -> button.setDisable(true));
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
