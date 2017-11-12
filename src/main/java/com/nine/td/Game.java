package com.nine.td;

import com.nine.td.game.graphics.map.Map;
import com.nine.td.game.playable.Engine;
import com.nine.td.game.playable.HasVariableSpeed;
import com.nine.td.game.ui.*;
import javafx.application.Platform;
import javafx.scene.Group;
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
import static java.util.function.Function.identity;

public class Game implements Engine, HasVariableSpeed {
    private static Game instance = null;

    private final Group mapPreview = new Group();
    private final List<Map> maps = new LinkedList<>();
    private final List<CssStyle> styles = new LinkedList<>();

    private final NavigationDisplay navigation;
    private final MenuBarDisplay menuBar;
    private final Scene scene;
    private final Player player;
    private final StatusBarDisplay statusBar;

    private final java.util.Map<GameEvent, List<Runnable>> eventHandlers =
            Stream  .of(GameEvent.values())
                    .collect(Collectors.toMap(identity(), e -> new LinkedList<>()));

    private String currentMapName;

    private Runnable gameSceneResizedEventHandler = () -> {};

    private Game() {
        this.navigation = new Navigation();
        this.menuBar = new MenuBar();
        this.player = new Player(100, 0, 500);
        this.statusBar = new StatusBar();
        this.scene = new Scene(new Group());

        this.statusBar.setPlayer(this.player);
    }

    public static Game getInstance() {
        synchronized(Game.class) {
            if(instance == null) {
                try {
                    instance = createGame();
                } catch (Exception e) {
                    throw new RuntimeException("Error creating game", e);
                }
            }

            return instance;
        }
    }

    private static Game createGame() throws Exception {
        Game game = new Game();

        //Threading resources fetching
        ExecutorService executors = Executors.newFixedThreadPool(2);

        //setting maps
        game.setMaps(executors.submit(() -> Files.list(GamePaths.MAPS).map(path -> path.toFile().getName()).collect(Collectors.toList())).get());

        //setting styles
        game.setStyles(executors.submit(() -> Files.list(GamePaths.STYLES).map(path -> new CssStyle(path.toFile().getName(), path.toFile().listFiles())).collect(Collectors.toList())).get());

        game.setStyle(DEFAULT_STYLE);

        return game;
    }

    @Override
    public void start() {
        if(this.getCurrentMap().isOver()) {
            this.reload();
        }
        this.getCurrentMap().start();
        this.trigger(GameEvent.START);
    }

    @Override
    public void pause() {
        this.getCurrentMap().pause();
        this.trigger(GameEvent.PAUSE);
    }

    @Override
    public void stop() {
        this.getCurrentMap().stop();
        this.trigger(GameEvent.STOP);
    }

    public void reload() {
        this.trigger(GameEvent.ENDED);
        this.stop();
        this.setMap(this.getCurrentMap().getName());
    }

    @Override
    public void changeSpeed(double coeff) {
        this.getCurrentMap().changeSpeed(coeff);
    }

    public Scene getScene() {
        if(this.scene.getRoot().getChildrenUnmodifiable().isEmpty()) {
            this.scene.setRoot(new VBox(this.menuBar.render(), this.navigation.render(), this.mapPreview, this.statusBar.render()));
            this.navigation.onMapSelected((observable, oldValue, newValue) -> this.setMap(newValue));
            this.menuBar.onThemeSwitch(event -> this.setStyle(((MenuItem) event.getSource()).getText()));
        }
        return this.scene;
    }

    public void onSceneUpdate(Runnable handler) {
        if(handler != null) {
            this.gameSceneResizedEventHandler = handler;
        }
    }

    private void setMaps(List<String> maps) {
        this.maps.addAll(maps.stream().map(Map::load).collect(Collectors.toList()));
        this.navigation.setMaps(maps);
        this.setMap(this.maps.get(0).getName());
    }

    private void setMap(String mapName) {
        this.currentMapName = mapName;

        Map map = this.getCurrentMap().reload();

        map.getWaves().forEach(wave -> wave.onWaveUpdate(() -> {
            if(wave.get().isEmpty()) {
                stop();
                map.loadNextWave();
            }

            if(!map.getCurrentWave().isPresent()) {
                trigger(GameEvent.ENDED);
            }

            this.statusBar.update();
        }));

        this.mapPreview.getChildren().setAll(map.render());
        this.statusBar.setMap(map);
        this.statusBar.update();
        this.fireEvent();
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

        this.fireEvent();
    }

    private CssStyle getStyle(String styleName) {
        return this.styles
                .stream()
                .filter(cssStyle -> cssStyle.getName().equals(styleName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Style " + styleName + " not found"));
    }

    public static void exit() {
        instance.stop();
        Platform.exit();
        System.exit(0);
    }

    public void onStart(Runnable handler) {
        this.addHandler(GameEvent.START, handler);
    }

    public void onPause(Runnable handler) {
        this.addHandler(GameEvent.PAUSE, handler);
    }

    public void onStop(Runnable handler) {
        this.addHandler(GameEvent.STOP, handler);
    }

    public void onEnded(Runnable handler) {
        this.addHandler(GameEvent.ENDED, handler);
    }

    private void addHandler(GameEvent event, Runnable runnable) {
        if(runnable != null) {
            this.eventHandlers.get(event).add(runnable);
        }
    }

    private void trigger(GameEvent gameEvent) {
        this.eventHandlers.get(gameEvent).forEach(Runnable::run);
    }

    private void fireEvent() {
        if(this.gameSceneResizedEventHandler != null) {
            this.gameSceneResizedEventHandler.run();
        }
    }

    enum GameEvent {
        START,
        PAUSE,
        STOP,
        ENDED;
    }
}
