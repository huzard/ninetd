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
import java.util.List;
import java.util.stream.Collectors;

public class AnimatedGraphicComponent extends GraphicComponent {
    private final List<Image> images;
    private double timeAnimation;
    private Timeline animationTimeline = new Timeline();
    private int currentImg = 0;

    public AnimatedGraphicComponent(Path supplier, Scale scale, double timeAnimation) {
        super(supplier, scale);
        this.images = this.loadImages();
        this.imgContainer.setImage(this.images.get(0));
        this.timeAnimation = timeAnimation;
        this.setAnimationTimeline(this.timeAnimation);
    }

    @Override
    public void start() {
        this.animationTimeline.play();
    }

    @Override
    public void stop() {
        this.animationTimeline.stop();
        this.currentImg = 0;
    }

    @Override
    public void pause() {
        this.animationTimeline.pause();
    }

    private List<Image> loadImages() {
        Preconditions.checkState(this.imgPath.toFile().exists(), "Invalid path to images : " + imgPath);

        if(this.imgPath.toFile().isDirectory()) {
            try {
                return Files.list(this.imgPath).map(p -> this.drawTools.loadImage(p)).collect(Collectors.toList());
            } catch(Exception e) {
                return Collections.singletonList(this.drawTools.loadImage(GamePaths.RESOURCES.resolve("null.png")));
            }
        } else {
            return Collections.singletonList(this.drawTools.loadImage(this.imgPath));
        }
    }

    private void setAnimationTimeline(double speed) {
        Preconditions.checkArgument(speed > 0, "invalid speed");

        if(this.animationTimeline != null) {
            this.animationTimeline.stop();
        }

        this.animationTimeline = new Timeline(
                new KeyFrame(
                        Duration.millis(speed),
                        e -> this.imgContainer.imageProperty().setValue(this.images.get(this.currentImg++ % this.images.size()))
                )
        );

        this.animationTimeline.setCycleCount(Animation.INDEFINITE);
    }

    protected void changeAnimationSpeed(double coeff) {
        this.setAnimationTimeline(this.timeAnimation / coeff);
    }
}
