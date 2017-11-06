package com.nine.td.game.ui;

import com.google.common.base.Preconditions;
import com.nine.td.Game;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nine.td.GameConstants.DEFAULT_STYLE;

public class MenuBar implements MenuBarDisplay {
    private Node root;

    private Menu themeSwitch;

    public MenuBar() {
        this.themeSwitch = new Menu("Themes");
    }

    @Override
    public Node render() {
        if(this.root == null) {
            //File menu
            MenuItem newGame = new MenuItem("Nouveau jeu");
            newGame.setOnAction(event -> Game.getInstance().reload());
            MenuItem exit = new MenuItem("Quitter");
            exit.setOnAction(event -> Game.exit());

            this.root = new javafx.scene.control.MenuBar(
                new Menu("Fichier", null, newGame, new SeparatorMenuItem(), exit),
                this.themeSwitch
            );
        }

        return this.root;
    }

    @Override
    public void setStyles(List<String> styleList) {
        Preconditions.checkArgument(styleList != null, "style list null");

        ToggleGroup toggleGroup = new ToggleGroup();
        RadioMenuItem defaultTheme = new RadioMenuItem(DEFAULT_STYLE);
        List<RadioMenuItem> themes = styleList.stream().map(RadioMenuItem::new).collect(Collectors.toList());

        Stream.concat(Stream.of(defaultTheme), themes.stream()).forEach(theme -> theme.setToggleGroup(toggleGroup));

        this.themeSwitch.getItems().add(defaultTheme);
        this.themeSwitch.getItems().add(new SeparatorMenuItem());
        this.themeSwitch.getItems().addAll(themes);

        defaultTheme.setSelected(true);
    }

    @Override
    public void onThemeSwitch(EventHandler<ActionEvent> styleChangedHandler) {
        this.themeSwitch.getItems().forEach(item -> item.addEventHandler(ActionEvent.ACTION, styleChangedHandler));
    }
}
