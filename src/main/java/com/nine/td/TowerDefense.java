package com.nine.td;

import com.nine.td.game.ui.Game;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nine.td.GamePaths.*;

public class TowerDefense extends Application {
    private Game game;

    public static void main(String[] args) {
        Application.launch(TowerDefense.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.game = Game.create();

        Scene scene = new Scene(this.game.render());

        this.game
                .getCssStyles()
                .forEach(css -> scene
                        .getStylesheets()
                        .addAll(Stream.of(css.getFiles())
                                .map(file -> TowerDefense.class.getResource(Stream.of(CSS_DIR, css.getName(), file.getName()).collect(Collectors.joining(FILE_SEP))).toExternalForm())
                                .collect(Collectors.toList())
                        )
                );


        primaryStage.setTitle("Tower Defense");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> this.game.stop());

        primaryStage.show();

//        this.game.start();
    }

    public static void exit() {
        Platform.exit();
        System.exit(0);
    }
}
