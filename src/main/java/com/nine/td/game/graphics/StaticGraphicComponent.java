package com.nine.td.game.graphics;

import com.google.common.base.Preconditions;
import javafx.scene.transform.Scale;

import java.nio.file.Path;

public class StaticGraphicComponent extends GraphicComponent {
    public StaticGraphicComponent(Path imgPath, Scale scale) {
        super(imgPath, scale);
        Preconditions.checkArgument(!imgPath.toFile().isDirectory(), "static img referred through directory");
        this.imgContainer.setImage(this.drawTools.loadImage(imgPath));
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void pause() {}
}
