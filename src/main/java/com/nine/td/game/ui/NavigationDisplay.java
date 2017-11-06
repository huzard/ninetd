package com.nine.td.game.ui;

import javafx.beans.value.ChangeListener;

import java.util.List;

public interface NavigationDisplay extends HasRendering {
    void setMaps(List<String> mapList);
    void onMapSelected(ChangeListener<String> mapSelectedHandler);
}
