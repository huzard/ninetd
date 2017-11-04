package com.nine.td.game.playable;

import com.google.common.base.Preconditions;
import com.nine.td.game.graphics.GraphicComponent;
import com.nine.td.game.path.Direction;
import com.nine.td.game.path.HasDirection;
import com.nine.td.game.path.Path;
import com.nine.td.game.path.Position;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * Représente une cible
 */
public class Target extends GraphicComponent implements HasDirection, Engine, Contains<Observer<Target>>, HasPath {
    private         Direction direction;
    protected       Position position = null;
    private         int life;
    private         int shield;
    private         int speed;
    protected       Path path;
    private         Timeline timeline;
    private final   List<Observer<Target>> observers = new CopyOnWriteArrayList<>();
    private boolean hasReachedEnd = false;

    public Target(Supplier<java.nio.file.Path> imgSupplier, int life, int shield, int speed) {
        super(imgSupplier);
        this.setLife(life);
        this.setShield(shield);
        this.setSpeed(speed);
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

        if(this.timeline == null) {
            this.position = this.path.getStart().getPosition();
            this.direction = this.path.getStart().getDirection();

            this.timeline = new Timeline(new KeyFrame(Duration.millis(this.speed), irrelevant -> {
                observers.forEach(observer -> observer.check(Target.this));

                if(isDown() || hasReachedEnd) {
                    stop();
                } else {
                    position.move(Target.this.direction);
                    path.accept(Target.this);
                    if(path.getEnd().getPosition().equals(position)) {
                        hasReachedEnd = true;
                    }
                }
            }));

            this.timeline.setCycleCount(Animation.INDEFINITE);
        }

        this.timeline.play();
    }

    @Override
    public void stop() {
        if(this.timeline != null) {
            this.timeline.stop();
        }
    }

    @Override
    public void pause() {
        this.timeline.pause();
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
}