package com.nine.td.game.graphics;

import com.nine.td.GameConstants;
import com.nine.td.GamePaths;
import com.nine.td.game.path.Position;
import com.nine.td.game.path.WayPoint;
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

    public GraphicComponent draw(WayPoint wayPoint) {
        return get(wayPoint.getDirection().charCode(), this.scale, wayPoint.getPosition());
    }

    public GraphicComponent drawTile(Position position) {
        return get(GameConstants.WALL_TILE, this.scale, position);
    }

    public GraphicComponent drawPath(Position position) {
        return get(GameConstants.PATH_TILE, this.scale, position);
    }

    public GraphicComponent draw(Unit unit) {
        return drawNull(unit.getPosition());
    }

    public GraphicComponent drawNull(Position position) {
        return get('-', this.scale, position);
    }

    private GraphicComponent get(char code, Scale scale, Position position) {
        GraphicComponent graphicComponent = new StaticGraphicComponent(GamePaths.SPRITES.resolve("null.png"), scale);

        switch(code) {
            case GameConstants.WALL_TILE    : graphicComponent = new StaticGraphicComponent(GamePaths.SPRITES.resolve("tile.png"), scale); break;
            case GameConstants.PATH_TILE    : graphicComponent = new StaticGraphicComponent(GamePaths.SPRITES.resolve("path.png"), scale); break;
            case GameConstants.NORTH_DIR    : graphicComponent = new AnimatedGraphicComponent(GamePaths.SPRITES.resolve("north"), scale, GameConstants.ANIMATION_WAYPOINT_SPEED); break;
            case GameConstants.EAST_DIR     : graphicComponent = new AnimatedGraphicComponent(GamePaths.SPRITES.resolve("east"), scale, GameConstants.ANIMATION_WAYPOINT_SPEED); break;
            case GameConstants.SOUTH_DIR    : graphicComponent = new AnimatedGraphicComponent(GamePaths.SPRITES.resolve("south"), scale, GameConstants.ANIMATION_WAYPOINT_SPEED); break;
            case GameConstants.WEST_DIR     : graphicComponent = new AnimatedGraphicComponent(GamePaths.SPRITES.resolve("west"), scale, GameConstants.ANIMATION_WAYPOINT_SPEED); break;
        }

        graphicComponent.setPosition(Position.nonNull(position));

        return graphicComponent;
    }
}
