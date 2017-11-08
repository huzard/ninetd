package com.nine.td.game.ui;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;

import java.util.List;

public interface NavigationDisplay extends HasRendering<Node> {
    void setMaps(List<String> mapList);
    void onMapSelected(ChangeListener<String> mapSelectedHandler);
}
