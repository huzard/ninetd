package com.nine.td.game.playable;

public interface Observer<D> {
    void notify(D data);
}
