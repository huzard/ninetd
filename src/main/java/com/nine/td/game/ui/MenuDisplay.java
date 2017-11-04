package com.nine.td.game.ui;

import com.nine.td.game.HasRendering;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public interface MenuDisplay extends HasRendering {
    void addStyle(String styleName);
    void addMap(String mapName);
    void onMapSelected(ChangeListener<String> mapSelectedHandler);
    void onGameStart(EventHandler<ActionEvent> handler);
}
