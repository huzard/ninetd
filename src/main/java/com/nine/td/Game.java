package com.nine.td;

import com.nine.td.game.graphics.map.Map;
import com.nine.td.game.playable.Engine;
import com.nine.td.game.ui.MenuBar;
import com.nine.td.game.ui.MenuBarDisplay;
import com.nine.td.game.ui.Navigation;
import com.nine.td.game.ui.NavigationDisplay;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;

import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nine.td.GameConstants.DEFAULT_STYLE;
import static com.nine.td.GamePaths.CSS_DIR;
import static com.nine.td.GamePaths.FILE_SEP;

public class Game implements Engine {
    private Node root;
    private final Group mapPreview = new Group();
    private final List<Map> maps = new LinkedList<>();
    private final List<CssStyle> styles = new LinkedList<>();

    private String currentMapName;
    private NavigationDisplay navigation;
    private MenuBarDisplay menuBar;
    private Scene scene;

    private static GameState STATE = GameState.STOPPED;

    private static Game instance = null;

    private Game() {
        this.navigation = new Navigation();
        this.menuBar = new MenuBar();
        this.scene = new Scene(new VBox(this.menuBar.render(), this.navigation.render(), this.mapPreview));
    }

    public static Game getInstance() {
        if(instance == null) {
            try {
                instance = createGame();
            } catch (Exception e) {
                throw new RuntimeException("Error creating game");
            }
        }

        return instance;
    }

    private static Game createGame() throws Exception {
        Game game = new Game();

        //Threading resources fetching
        ExecutorService executors = Executors.newFixedThreadPool(2);

        //setting maps
        game.setMaps(executors.submit(() -> Files.list(GamePaths.MAPS).map(path -> path.toFile().getName()).collect(Collectors.toList())).get());

        //setting styles
        game.setStyles(executors.submit(() -> Files.list(GamePaths.STYLES).map(path -> new CssStyle(path.toFile().getName(), path.toFile().listFiles())).collect(Collectors.toList())).get());

        //setting handlers
        game.navigation.onMapSelected((observable, oldValue, newValue) -> game.setMap(newValue));

        game.menuBar.onThemeSwitch(event -> game.setStyle(((MenuItem) event.getSource()).getText()));

        game.setStyle(DEFAULT_STYLE);

        return game;
    }

    @Override
    public void start() {
        this.getCurrentMap().reload().start();
        STATE = GameState.RUNNING;
    }

    @Override
    public void pause() {
        this.getCurrentMap().pause();
        STATE = GameState.PAUSED;
    }

    @Override
    public void stop() {
        this.getCurrentMap().stop();
        STATE = GameState.STOPPED;
    }

    public Scene getScene() {
        return this.scene;
    }

    public void onSceneUpdate(EventHandler<ActionEvent> handler) {
        this.menuBar.onThemeSwitch(handler);
    }

    private void setMaps(List<String> maps) {
        this.maps.addAll(maps.stream().map(Map::load).collect(Collectors.toList()));
        this.navigation.setMaps(maps);
        this.setMap(this.maps.get(0).getName());
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

    private void setStyles(List<CssStyle> styles) {
        this.styles.addAll(styles);
        this.menuBar.setStyles(styles.stream().map(CssStyle::getName).collect(Collectors.toList()));
    }

    private void setStyle(String style) {
        if(DEFAULT_STYLE.equals(style)) {
            this.scene.getStylesheets().clear();
        } else {
            CssStyle css = this.getStyle(style);

            this.scene.getStylesheets().setAll(
                    Stream
                            .of(css.getFiles())
                            .map(file -> TowerDefense.class.getResource(String.join(FILE_SEP, CSS_DIR, css.getName(), file.getName())).toExternalForm())
                            .collect(Collectors.toList())
            );
        }
    }

    private CssStyle getStyle(String styleName) {
        return this.styles
                .stream()
                .filter(cssStyle -> cssStyle.getName().equals(styleName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Style " + styleName + " not found"));
    }

    public static void exit() {
        Platform.exit();
        System.exit(0);
    }

    enum GameState { RUNNING, PAUSED, STOPPED; }
}
