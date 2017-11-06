package com.nine.td.game.path;

import java.io.Serializable;

/**
 * Représente une position dans le plan
 */
public final class Position implements Serializable {
    //position x
    private double x;

    //position y
    private double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public Position setX(double x) {
        this.x = x;
        return this;
    }

    public double getY() {
        return y;
    }

    public Position setY(double y) {
        this.y = y;
        return this;
    }

    public double distanceWith(Position position) {
        double xDiff = Math.abs(this.x - position.x);
        double yDiff = Math.abs(this.y - position.y);
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
    }

    public boolean near(Position other, double delta) {
        return other != null && delta >= 0 && this.distanceWith(other) < delta;
    }

    public static Position nonNull(Position position) {
        return position == null ? new Position(0, 0) : position;
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (Double.compare(position.x, x) != 0) return false;
        return Double.compare(position.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
