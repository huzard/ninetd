package com.nine.td.game.path;

import com.google.common.base.Preconditions;
import com.nine.td.GameConstants;

import java.io.Serializable;

/**
 * Représente une direction dans un plan orienté, définie par une valeur (x; y)
 */
public enum Direction implements Serializable {
    NORTH   (0, -1, GameConstants.NORTH_DIR),    //Déplacement vertical haut     : aucun shift x, un déplacement vertical vers le haut
    EAST    (1, 0,  GameConstants.EAST_DIR),     //Déplacement horizontal droit  : un déplacement latéral vers la droite, aucun shift y
    SOUTH   (0, 1,  GameConstants.SOUTH_DIR),     //Déplacement vertical bas      : aucun shift x, un déplacement vertical vers le bas
    WEST    (-1, 0, GameConstants.WEST_DIR);    //Déplacement horizontal gauche : un déplacement latéral vers la droite, aucun shift y

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
            case GameConstants.NORTH_DIR    : return NORTH;
            case GameConstants.EAST_DIR     : return EAST;
            case GameConstants.SOUTH_DIR    : return SOUTH;
            case GameConstants.WEST_DIR     : return WEST;
        }

        throw new IllegalArgumentException(String.format("Unrecognized direction : %c. Required one among N, E, S, or W.", value));
    }

    public void move(Position position) {
        Preconditions.checkArgument(position != null, "null position");
        position.setX(position.getX() + this.getXShift());
        position.setY(position.getY() + this.getYShift());
    }
}
