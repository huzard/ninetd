package com.nine.td.game.path;

import com.google.common.base.Preconditions;

import java.io.Serializable;

/**
 * Représente une direction dans un plan orienté, définie par une valeur (x; y)
 */
public enum Direction implements Serializable {
    NORTH   (0, -1, 'N'),    //Déplacement vertical haut     : aucun shift x, un déplacement vertical vers le haut
    EAST    (1, 0, 'E'),     //Déplacement horizontal droit  : un déplacement latéral vers la droite, aucun shift y
    SOUTH   (0, 1, 'S'),     //Déplacement vertical bas      : aucun shift x, un déplacement vertical vers le bas
    WEST    (-1, 0, 'W');    //Déplacement horizontal gauche : un déplacement latéral vers la droite, aucun shift y

    //x shift
    private int xShift;

    //y shift
    private int yShift;

    //char code
    private char charCode;

    Direction(int xShift, int yShift, char charCode) {
        this.xShift = xShift;
        this.yShift = yShift;
        this.charCode = charCode;
    }

    public int getXShift() {
        return xShift;
    }

    public int getYShift() {
        return yShift;
    }

    public char charCode() {
        return charCode;
    }

    public void update(HasDirection hasDirection) {
        hasDirection.setDirection(this);
    }

    public static Direction get(char value) {
        switch(value) {
            case 'N' : return NORTH;
            case 'E' : return EAST;
            case 'S' : return SOUTH;
            case 'W' : return WEST;
        }

        throw new IllegalArgumentException(String.format("Unrecognized direction : %c. Required one among N, E, S, or W.", value));
    }

    public void move(Position position) {
        Preconditions.checkArgument(position != null, "null position");
        position.setX(position.getX() + this.getXShift());
        position.setY(position.getY() + this.getYShift());
    }
}
