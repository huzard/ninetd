package com.nine.td;

import com.google.common.base.Preconditions;
import com.nine.td.game.graphics.map.Map;
import com.nine.td.game.playable.Engine;
import com.nine.td.game.ui.Menu;
import com.nine.td.game.ui.MenuDisplay;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nine.td.GamePaths.CSS_DIR;
import static com.nine.td.GamePaths.FILE_SEP;

public class Game extends Application implements Engine {
    private final Group mapPreview = new Group();
    private final List<Map> maps = new LinkedList<>();
    private final List<CssStyle> styles = new LinkedList<>();

    private String currentMapName;
    private final MenuDisplay menu;
    private final Scene scene;

    public Game() {
        this.menu = new Menu();
        this.scene = new Scene(new VBox(this.menu.render(), this.mapPreview));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Threading resources fetching
        ExecutorService executors = Executors.newFixedThreadPool(2);

        //setting maps
        this.setMaps(executors.submit(() -> Files.list(GamePaths.MAPS).map(path -> path.toFile().getName()).collect(Collectors.toList())).get());

        //setting styles
        this.setStyles(executors.submit(() -> Files.list(GamePaths.STYLES).map(path -> new CssStyle(path.toFile().getName(), path.toFile().listFiles())).collect(Collectors.toList())).get());

        //setting handlers
        this.menu.onMapSelected((observable, oldValue, newValue) -> setMap(newValue));
        this.menu.onGameStart(event -> start());
        this.menu.onThemeSwitch((observable, oldValue, newValue) -> {
            setStyle(newValue);
            primaryStage.sizeToScene();
        });

        //Tuning stage
        primaryStage.setTitle("Tower Defense");
        primaryStage.setScene(this.scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> this.stop());
        primaryStage.show();
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }

    @Override
    public void start() {
        Preconditions.checkState(!this.maps.isEmpty(), "no maps loaded");
        this.getCurrentMap().reload().start();
    }

    @Override
    public void stop() {
        this.getCurrentMap().stop();
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void pause() {
        this.getCurrentMap().pause();
    }

    private void setMaps(List<String> maps) {
        this.maps.addAll(maps.stream().map(Map::load).collect(Collectors.toList()));
        this.menu.setMaps(maps);
        this.setMap(this.maps.get(0).getName());
    }

    private void setStyles(List<CssStyle> styles) {
        this.styles.addAll(styles);
        this.menu.setStyles(styles.stream().map(CssStyle::getName).collect(Collectors.toList()));
        this.setStyle(this.styles.get(0).getName());
    }

    private void setMap(String mapName) {
        this.currentMapName = mapName;
        this.mapPreview.getChildren().setAll(this.getMap(mapName).render());
    }

    private Map getMap(String mapName) {
        return this.maps
                .stream()
                .filter(map -> map.getName().equals(mapName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Map " + mapName + " not found"));
    }

    private Map getCurrentMap() {
        return this.getMap(this.currentMapName);
    }

    private void setStyle(String style) {
        CssStyle css = this.getStyle(style);

        this.scene.getStylesheets().setAll(
                Stream
                        .of(css.getFiles())
                        .map(file -> TowerDefense.class.getResource(String.join(FILE_SEP, CSS_DIR, css.getName(), file.getName())).toExternalForm())
                        .collect(Collectors.toList())
        );
    }

    private CssStyle getStyle(String styleName) {
        return this.styles
                .stream()
                .filter(cssStyle -> cssStyle.getName().equals(styleName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Style " + styleName + " not found"));
    }
}
