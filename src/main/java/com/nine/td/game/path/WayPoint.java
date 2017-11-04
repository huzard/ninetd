package com.nine.td.game.path;

import java.util.Objects;

/**
 * Représente un point de contrôle qui sera en charge de modifier le trajet d'une unité
 */
public class WayPoint implements HasPosition, HasDirection {
    private Direction direction;
    private Position position;

    public WayPoint(Direction direction, Position position) {
        this.setPosition(position);
        this.setDirection(direction);
    }

    @Override
    public Position getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "position must not be null");
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
    }
}
