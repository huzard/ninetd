package com.nine.td.game.graphics;

import com.google.common.base.Preconditions;
import com.nine.td.game.path.HasPosition;
import com.nine.td.game.path.Position;
import com.nine.td.game.playable.Engine;
import com.nine.td.game.ui.HasRendering;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Scale;

import java.nio.file.Path;
import java.util.Objects;

public abstract class GraphicComponent implements Engine, HasPosition, HasRendering<ImageView> {
    protected final ImageView imgContainer = new ImageView();
    protected       Position position = new Position(0, 0);
    protected       DrawTools drawTools;
    protected       Path imgPath;

    public GraphicComponent(Path imgPath, Scale scale) {
        Preconditions.checkArgument(imgPath != null, "null image path");
        Preconditions.checkArgument(scale != null, "null scale");
        Preconditions.checkArgument(imgPath.toFile().exists(), "image path does not exist");
        Preconditions.checkArgument(scale.getX() > 0 && scale.getY() > 0, "invalid scale");

        this.drawTools = new DrawTools(scale);
        this.imgPath = imgPath;
    }

    @Override
    public Position getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "null position");
    }

    @Override
    public ImageView render() {
        if(this.position != null) {
            this.imgContainer.relocate(this.getPosition().getX(), this.getPosition().getY());
        }

        return this.imgContainer;
    }
}
