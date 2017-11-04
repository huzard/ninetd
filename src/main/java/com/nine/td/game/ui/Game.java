package com.nine.td.game.ui;

import com.google.common.base.Preconditions;
import com.nine.td.CssStyle;
import com.nine.td.GamePaths;
import com.nine.td.TowerDefense;
import com.nine.td.game.HasRendering;
import com.nine.td.game.graphics.map.Map;
import com.nine.td.game.playable.Engine;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

public class Game implements Engine, HasRendering {
    private Group root;
    private Group mapPreview = new Group();

    private String currentMapName;

    private final List<Map> maps = new LinkedList<>();
    private final List<CssStyle> styles = new LinkedList<>();
    private final MenuDisplay menu = new Menu();

    private Game() {
        this.menu.onMapSelected((observable, oldValue, newValue) -> {
            try {
                this.currentMapName = newValue;
                loadPreview();
            } catch (Exception e) {
                System.err.println("Error loading map preview " + newValue);
            }
        });

        this.menu.onGameStart(event -> start());
    }

    public static Game create() throws Exception {
        Game game = new Game();

        //getting maps
        Files.newDirectoryStream(GamePaths.MAPS).forEach(mapFile -> game.addMap(mapFile.toFile().getName()));

        //getting styles
        Files.newDirectoryStream(GamePaths.STYLES).forEach(dir -> game.addStyle(new CssStyle(dir.toFile().getName(), dir.toFile().listFiles())));

        return game;
    }

    private void addMap(String fileName) {
        try {
            this.maps.add(Map.load(fileName));
            this.menu.addMap(fileName);

            if(this.maps.size() == 1) {
                this.currentMapName = fileName;
            }
        } catch (IOException e) {
            System.out.println("Error adding map " + fileName + " : " + e.getMessage());
        }
    }

    private void addStyle(CssStyle style) {
        this.styles.add(style);
        this.menu.addStyle(style.getName());
    }

    @Override
    public void start() {
        Preconditions.checkState(!this.maps.isEmpty(), "no maps loaded");
        try {
            this.getCurrentMap().reload().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        this.getCurrentMap().stop();
        TowerDefense.exit();
    }

    @Override
    public void pause() {
        this.getCurrentMap().pause();
    }

    @Override
    public Group render() throws Exception {
        if(this.root == null) {
            HBox hBox = new HBox(this.mapPreview, this.menu.render());
            hBox.setSpacing(5.0);
            this.loadPreview();
            this.root = new Group(hBox);
        }

        return this.root;
    }

    private void loadPreview() throws Exception {
        this.mapPreview.getChildren().clear();
        this.mapPreview.getChildren().add(this.getCurrentMap().render());
    }

    private Map getCurrentMap() {
        return this.maps
                .stream()
                .filter(map -> map.getName().equals(this.currentMapName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Map not found"));
    }

    public List<CssStyle> getCssStyles() {
        return this.styles;
    }
}
