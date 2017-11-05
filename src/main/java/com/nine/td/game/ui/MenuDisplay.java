package com.nine.td.game.ui;

import com.nine.td.game.HasRendering;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.List;

public interface MenuDisplay extends HasRendering {
    void setStyles(List<String> styleList);
    void setMaps(List<String> mapList);
    void onMapSelected(ChangeListener<String> mapSelectedHandler);
    void onGameStart(EventHandler<ActionEvent> handler);
    void onThemeSwitch(ChangeListener<String> styleChangedHandler);
}
