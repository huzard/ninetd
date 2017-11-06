package com.nine.td.game.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.List;

public interface MenuBarDisplay extends HasRendering {
    void setStyles(List<String> styleList);
    void onThemeSwitch(EventHandler<ActionEvent> styleChangedHandler);
}
