package com.nine.td.game.ui;

import javafx.scene.Node;

public interface HasRendering<T extends Node> {
    T render();
}
