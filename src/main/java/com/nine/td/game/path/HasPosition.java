package com.nine.td.game.path;

public interface HasPosition {
    Position getPosition();
    void setPosition(Position position);

    default void setPosition(double x, double y) {
        setPosition(new Position(x, y));
    }
}
