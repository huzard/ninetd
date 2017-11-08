package com.nine.td.game.graphics;

import com.nine.td.GamePaths;
import com.nine.td.game.path.Position;

public final class Components {
    private Components() {
        //nothing
    }

    public static GraphicComponent get(char code, Position position) {
        GraphicComponent graphicComponent = new GraphicComponent(GamePaths.SPRITES.resolve("null.png"));

        switch(code) {
            case '0' : graphicComponent = new GraphicComponent(GamePaths.SPRITES.resolve("tile.png")); break;
            case '1' : graphicComponent = new GraphicComponent(GamePaths.SPRITES.resolve("path.png")); break;
        }

        graphicComponent.setPosition(Position.nonNull(position));

        return graphicComponent;
    }

    public static GraphicComponent get(char code) {
        return get(code, new Position(0, 0));
    }
}
