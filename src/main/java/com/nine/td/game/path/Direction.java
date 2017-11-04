package com.nine.td.game.path;

import java.io.Serializable;

/**
 * Représente une direction dans un plan orienté, définie par une valeur (x; y)
 */
public enum Direction implements Serializable {
    NORTH   (0, -1),    //Déplacement vertical haut     : aucun shift x, un déplacement vertical vers le haut
    EAST    (1, 0),     //Déplacement horizontal droit  : un déplacement latéral vers la droite, aucun shift y
    SOUTH   (0, 1),     //Déplacement vertical bas      : aucun shift x, un déplacement vertical vers le bas
    WEST    (-1, 0);    //Déplacement horizontal gauche : un déplacement latéral vers la droite, aucun shift y

    //x shift
    private int xShift;

    //y shift
    private int yShift;

    Direction(int xShift, int yShift) {
        this.xShift = xShift;
        this.yShift = yShift;
    }

    public int getXShift() {
        return xShift;
    }

    public int getYShift() {
        return yShift;
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
}
