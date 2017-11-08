package com.nine.td.game.graphics;

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
            case '0' : graphicComponent = new StaticGraphicComponent(GamePaths.SPRITES.resolve("tile.png"), scale); break;
            case '1' : graphicComponent = new StaticGraphicComponent(GamePaths.SPRITES.resolve("path.png"), scale); break;
        }

        graphicComponent.setPosition(Position.nonNull(position));

        return graphicComponent;
    }

    public static GraphicComponent get(char code, Scale scale) {
        return get(code, scale, new Position(0, 0));
    }
}
