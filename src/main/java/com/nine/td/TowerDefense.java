package com.nine.td;

import javafx.application.Application;
import javafx.stage.Stage;

public class TowerDefense extends Application {
    public static void main(String[] args) {
        Application.launch(TowerDefense.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Game game = Game.getInstance();

        game.onSceneUpdate(primaryStage::sizeToScene);

        //Tuning stage
        primaryStage.setTitle("Tower Defense");
        primaryStage.setScene(game.getScene());
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> Game.exit());

        primaryStage.show();
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();

    }
}
