package com.nine.td.game.playable;

import com.google.common.base.Preconditions;
import com.nine.td.GameConstants;
import com.nine.td.GamePaths;
import com.nine.td.game.HasRendering;
import com.nine.td.game.path.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.nine.td.GameConstants.ANIMATION_TARGET_SPEED;
import static com.nine.td.GameConstants.REQUIRED_SIZE;

/**
 * Représente une cible
 */
public class Target implements HasRendering, HasPosition, HasDirection, Engine, Contains<Observer<Target>>, HasPath, HasVariableSpeed {
    private         Direction direction;
    protected       Position position = null;
    private         int life;
    private         int shield;
    private         int speed;
    protected       Path path;
    private         Timeline movementTimeline;
    private         Timeline animationTimeline;
    private final   List<Observer<Target>> observers = new CopyOnWriteArrayList<>();
    private final   List<ImageView> images = new LinkedList<>();
    private         boolean hasReachedEnd = false;
    private final   Group imgContainer = new Group();

    private int     currentImg = 0;

    public Target(Scale scale, java.nio.file.Path imgPath, int life, int shield, int speed) {
        this.setLife(life);
        this.setShield(shield);
        this.setSpeed(speed);
        this.loadImages(scale, imgPath);

        this.setMovementTimeline(speed);
        this.setAnimationTimeline(GameConstants.ANIMATION_TARGET_SPEED);
    }

    private void loadImages(Scale scale, java.nio.file.Path path) {
        this.images.clear();

        Function<java.nio.file.Path, ImageView> transform = p -> new ImageView(new Image(
                String.format("file:///%s", p),
                REQUIRED_SIZE * scale.getX(),
                REQUIRED_SIZE * scale.getY(),
                false,
                true)
        );

        Preconditions.checkState(path.toFile().exists(), "Invalid path to images : " + path);

        if(path.toFile().isDirectory()) {
            try {
                this.images.addAll(Files.list(path).map(transform).collect(Collectors.toList()));
            } catch(Exception e) {
                loadImages(scale, GamePaths.RESOURCES.resolve("null.png"));
            }
        } else {
            this.images.add(transform.apply(path));
        }

        Preconditions.checkState(!this.images.isEmpty(), "Invalid path to images : " + path);
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = Objects.requireNonNull(direction);
    }

    @Override
    public Position getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position);
    }

    @Override
    public void start() {
        Preconditions.checkState(this.path != null, "null path");
        Preconditions.checkState(this.path.isValid(), "invalid path");

        if(this.position == null) {
            this.position = this.path.getStart().getPosition();
            this.direction = this.path.getStart().getDirection();
        }

        this.movementTimeline.play();
        this.animationTimeline.play();
    }

    @Override
    public void stop() {
        this.movementTimeline.stop();
        this.animationTimeline.stop();
        this.currentImg = 0;
    }

    @Override
    public void pause() {
        this.movementTimeline.pause();
        this.animationTimeline.pause();
    }

    @Override
    public boolean add(Observer<Target> enemy) {
        return this.observers.add(enemy);
    }

    @Override
    public boolean remove(Observer<Target> enemy) {
        return this.observers.remove(enemy);
    }

    @Override
    public List<Observer<Target>> get() {
        return observers;
    }

    @Override
    public Path getPath() {
        return this.path;
    }

    @Override
    public void setPath(Path path) {
        this.path = path;
    }

    public int getLife() {
        return this.life;
    }

    public Target setLife(int life) {
        this.life = Integer.max(0, life);
        return this;
    }

    public int getShield() {
        return this.shield;
    }

    public Target setShield(int shield) {
        this.shield = Integer.max(0, shield);
        return this;
    }

    public int getSpeed() {
        return speed;
    }

    public Target setSpeed(int speed) {
        this.speed = Integer.max(1, speed);
        return this;
    }

    public boolean isDown() {
        return this.life <= 0;
    }

    public void check() {
        this.observers.forEach(unit -> unit.check(this));
    }

    public boolean hasReachedEnd() {
        return this.hasReachedEnd;
    }

    public boolean hasMoved() {
        return this.position != null;
    }

    @Override
    public void changeSpeed(double coeff) {
        this.setMovementTimeline(this.speed / coeff);
        this.setAnimationTimeline(ANIMATION_TARGET_SPEED / coeff);

        if(hasMoved()) {
            start();
        }
    }

    private void setMovementTimeline(double speed) {
        Preconditions.checkArgument(speed > 0, "invalid speed");

        if(this.movementTimeline != null) {
            this.movementTimeline.stop();
        }

        this.movementTimeline = new Timeline(new KeyFrame(Duration.millis(speed), irrelevant -> {
            this.observers.forEach(observer -> observer.check(Target.this));

            if(isDown() || this.hasReachedEnd) {
                this.stop();
                this.imgContainer.getChildren().clear();
                this.imgContainer.setVisible(false);
            } else {
                this.direction.move(this.position);
                this.imgContainer.relocate(this.position.getX(), this.position.getY());

                this.path.accept(Target.this);

                if (this.path.getEnd().getPosition().equals(this.position)) {
                    this.hasReachedEnd = true;
                }
            }
        }));

        this.movementTimeline.setCycleCount(Animation.INDEFINITE);
    }

    private void setAnimationTimeline(double speed) {
        Preconditions.checkArgument(speed > 0, "invalid speed");

        if(this.animationTimeline != null) {
            this.animationTimeline.stop();
        }

        this.animationTimeline = new Timeline(new KeyFrame(Duration.millis(speed), irrelevant -> {
            this.imgContainer.getChildren().setAll(this.images.get(this.currentImg++ % this.images.size()));
        }));

        this.animationTimeline.setCycleCount(Animation.INDEFINITE);
    }

    @Override
    public Node render() {
        return this.imgContainer;
    }
}