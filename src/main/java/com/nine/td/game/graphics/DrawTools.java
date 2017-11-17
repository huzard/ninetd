package com.nine.td.game.graphics;

import com.nine.td.GameConstants;
import com.nine.td.game.playable.unit.Unit;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Scale;

public class DrawTools {
    private final Scale scale;

    public DrawTools(Scale scale) {
        this.scale = scale;
    }

    public Group createRangeCircle(Unit unit) {
        double x = unit.getPosition().getX() + (GameConstants.REQUIRED_SIZE * this.scale.getX()) / 2.0;
        double y = unit.getPosition().getY() + (GameConstants.REQUIRED_SIZE * this.scale.getY()) / 2.0;

        //Disk
        Circle disk = new Circle(x, y, unit.getRange(), Color.valueOf("#FF000020"));

        //Contour
        Circle contour = new Circle(x, y, unit.getRange());
        contour.setFill(Color.TRANSPARENT);
        contour.setStroke(Color.RED);

        return new Group(disk, contour);
    }
}
