package com.nine.td.game.ui;

import com.nine.td.Player;
import com.nine.td.game.graphics.map.Map;
import javafx.scene.Node;

public interface StatusBarDisplay extends HasRendering<Node> {
    void setPlayer(Player player);
    void setMap(Map map);
    void update();
}
