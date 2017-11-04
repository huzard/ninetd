package com.nine.td.game.graphics;

import com.google.common.base.Preconditions;
import com.nine.td.GamePaths;
import com.nine.td.game.path.HasPosition;
import com.nine.td.game.path.Position;
import javafx.scene.image.Image;
import javafx.scene.transform.Scale;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

import static com.nine.td.GameConstants.REQUIRED_SIZE;

public class GraphicComponent implements HasPosition {
    private Supplier<Path> supplier;
    private Position position = new Position(0, 0);

    public GraphicComponent(Supplier<Path> supplier) {
        this.supplier = supplier;
    }

    public GraphicComponent() {
        this(() -> GamePaths.SPRITES.resolve("null.png"));
    }

    public Image draw(Scale scale) {
        Preconditions.checkArgument(scale != null, "null scale");
        Preconditions.checkArgument(scale.getX() > 0, "invalid scale x, require value > 0, found " + scale.getX());
        Preconditions.checkArgument(scale.getY() > 0, "invalid scale y, require value > 0, found " + scale.getY());

        return new Image(
                String.format("file:///%s", this.supplier.get()),
                REQUIRED_SIZE * scale.getX(),
                REQUIRED_SIZE * scale.getY(),
                false,
                true
        );
    }

    public Image draw(double scaleX, double scaleY) {
        return draw(new Scale(scaleX, scaleY));
    }

    public Image draw() {
        return draw(1.0, 1.0);
    }

    @Override
    public Position getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "null position");
    }
}
