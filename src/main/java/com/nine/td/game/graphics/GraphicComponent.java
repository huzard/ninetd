package com.nine.td.game.graphics;

import com.google.common.base.Preconditions;
import com.nine.td.GameConstants;
import com.nine.td.GamePaths;
import com.nine.td.game.path.HasPosition;
import com.nine.td.game.path.Position;
import com.nine.td.game.ui.HasRendering;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.nine.td.GameConstants.ANIMATION_TARGET_SPEED;
import static com.nine.td.GameConstants.REQUIRED_SIZE;

public class GraphicComponent implements HasPosition, HasRendering<ImageView> {
    protected final List<Image> images;
    protected final ImageView imgContainer = new ImageView();
    private         Timeline animationTimeline = new Timeline();
    protected       Position position = new Position(0, 0);
    protected       int currentImg = 0;

    public GraphicComponent(Path supplier, Scale scale) {
        this.images = this.loadImages(scale, supplier);
        this.animationTimeline.setCycleCount(0);

        if(this.images.size() > 1) {
            this.setAnimationTimeline(GameConstants.ANIMATION_TARGET_SPEED);
        }

        this.imgContainer.setImage(this.images.get(0));
    }

    public GraphicComponent(Path supplier) {
        this(supplier, new Scale(1.0, 1.0));
    }

    @Override
    public Position getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "null position");
    }

    private List<Image> loadImages(Scale scale, java.nio.file.Path path) {
        List<Image> images = new LinkedList<>();

        Function<Path, Image> transform = p -> new Image(
                String.format("file:///%s", p),
                REQUIRED_SIZE * scale.getX(),
                REQUIRED_SIZE * scale.getY(),
                false,
                true
        );

        Preconditions.checkState(path.toFile().exists(), "Invalid path to images : " + path);

        if(path.toFile().isDirectory()) {
            try {
                images.addAll(Files.list(path).map(transform).collect(Collectors.toList()));
            } catch(Exception e) {
                return loadImages(scale, GamePaths.RESOURCES.resolve("null.png"));
            }
        } else {
            images.add(transform.apply(path));
        }

        Preconditions.checkState(!images.isEmpty(), "Invalid path to images : " + path);

        return images;
    }

    private void setAnimationTimeline(double speed) {
        Preconditions.checkArgument(speed > 0, "invalid speed");

        if(this.animationTimeline != null) {
            this.animationTimeline.stop();
        }

        this.animationTimeline = new Timeline(new KeyFrame(Duration.millis(speed), irrelevant -> {
            this.imgContainer.imageProperty().setValue(this.images.get(this.currentImg++ % this.images.size()));
        }));

        this.animationTimeline.setCycleCount(Animation.INDEFINITE);
    }

    @Override
    public ImageView render() {
        return this.imgContainer;
    }

    public void changeAnimationSpeed(double coeff) {
        this.setAnimationTimeline(ANIMATION_TARGET_SPEED / coeff);
    }

    protected void startAnimation() {
        this.animationTimeline.play();
    }

    protected void pauseAnimation() {
        this.animationTimeline.pause();
    }

    protected void stopAnimation() {
        this.animationTimeline.stop();
        this.currentImg = 0;
    }

    public void setVisible(boolean visible) {
        this.imgContainer.setVisible(visible);
    }

    public boolean isVisible() {
        return this.imgContainer.isVisible();
    }
}
