package com.nine.td.game.ui;

import com.nine.td.Player;
import com.nine.td.game.graphics.map.Map;

public interface StatusBarDisplay extends HasRendering {
    void setPlayer(Player player);
    void setMap(Map map);
    void update();
}
