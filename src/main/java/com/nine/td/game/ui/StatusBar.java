package com.nine.td.game.ui;

import com.nine.td.Player;
import com.nine.td.game.graphics.map.Map;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.Objects;

public class StatusBar implements StatusBarDisplay {
    private Node root;

    private final HBox playerInfos  = new HBox();
    private final HBox mapInfos    = new HBox();

    private Player player;
    private Map map;

    public StatusBar() {}

    @Override
    public Node render() {
        if(this.root == null) {
            this.update();
            this.root = new HBox(this.playerInfos, separator(), this.mapInfos, separator());
        }

        return this.root;
    }

    @Override
    public void setPlayer(Player player) {
        this.player = Objects.requireNonNull(player, "null player");
    }

    @Override
    public void setMap(Map map) {
        this.map = Objects.requireNonNull(map, "null map");
    }

    @Override
    public void update() {
        this.playerInfos.getChildren().setAll(new Text(String.format("%d/%d/%d", this.player.getHealth(), this.player.getMoney(), this.player.getScore())));

        this.mapInfos.getChildren().setAll(
                new Text("Map : " + this.map.getName()),
                separator(),
                new Text(this.map.getWaves().isEmpty() ? "Map over" : String.format("%d waves left. Current : %d enemies left", this.map.getWaves().size(), this.map.getCurrentWave().get().size()))
        );
    }

    private Node separator() {
        return new Separator(Orientation.VERTICAL);
    }
}
