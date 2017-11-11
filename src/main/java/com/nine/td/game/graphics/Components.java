package com.nine.td.game.graphics;

import com.nine.td.GameConstants;
import com.nine.td.GamePaths;
import com.nine.td.game.path.Position;
import javafx.scene.transform.Scale;

public final class Components {
    private Components() {
        //nothing
    }

    public static GraphicComponent get(char code, Scale scale, Position position) {
        GraphicComponent graphicComponent = new StaticGraphicComponent(GamePaths.SPRITES.resolve("null.png"), scale);

        switch(code) {
            case '#' : graphicComponent = new StaticGraphicComponent(GamePaths.SPRITES.resolve("tile.png"), scale); break;
            case '.' : graphicComponent = new StaticGraphicComponent(GamePaths.SPRITES.resolve("path.png"), scale); break;
            case 'N' : graphicComponent = new AnimatedGraphicComponent(GamePaths.SPRITES.resolve("north"), scale, GameConstants.ANIMATION_WAYPOINT_SPEED); break;
            case 'E' : graphicComponent = new AnimatedGraphicComponent(GamePaths.SPRITES.resolve("east"), scale, GameConstants.ANIMATION_WAYPOINT_SPEED); break;
            case 'S' : graphicComponent = new AnimatedGraphicComponent(GamePaths.SPRITES.resolve("south"), scale, GameConstants.ANIMATION_WAYPOINT_SPEED); break;
            case 'W' : graphicComponent = new AnimatedGraphicComponent(GamePaths.SPRITES.resolve("west"), scale, GameConstants.ANIMATION_WAYPOINT_SPEED); break;
        }

        graphicComponent.setPosition(Position.nonNull(position));

        return graphicComponent;
    }

    public static GraphicComponent get(char code, Scale scale) {
        return get(code, scale, new Position(0, 0));
    }
}
