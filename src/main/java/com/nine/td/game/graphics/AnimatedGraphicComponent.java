package com.nine.td.game.graphics;

import com.google.common.base.Preconditions;
import com.nine.td.GamePaths;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.nine.td.GameConstants.ANIMATION_TARGET_SPEED;

public class AnimatedGraphicComponent extends GraphicComponent {
    private final List<Image> images;
    private Timeline animationTimeline = new Timeline();
    private int currentImg = 0;

    public AnimatedGraphicComponent(Path supplier, Scale scale) {
        super(supplier, scale);
        this.images = this.loadImages();
        this.imgContainer.setImage(this.images.get(0));
        this.setAnimationTimeline(ANIMATION_TARGET_SPEED);
    }

    private List<Image> loadImages() {
        List<Image> images = new LinkedList<>();

        Preconditions.checkState(imgPath.toFile().exists(), "Invalid path to images : " + imgPath);

        if(imgPath.toFile().isDirectory()) {
            try {
                images.addAll(Files.list(imgPath).map(p -> loadImage(p, scale)).collect(Collectors.toList()));
            } catch(Exception e) {
                return Collections.singletonList(this.loadImage(GamePaths.RESOURCES.resolve("null.png"), scale));
            }
        } else {
            images.add(loadImage(imgPath, scale));
        }

        Preconditions.checkState(!images.isEmpty(), "Invalid path to images : " + imgPath);

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
}
