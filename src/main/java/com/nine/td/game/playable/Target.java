package com.nine.td.game.playable;

import com.google.common.base.Preconditions;
import com.nine.td.GameConstants;
import com.nine.td.game.graphics.AnimatedGraphicComponent;
import com.nine.td.game.path.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Représente une cible
 */
public class Target extends AnimatedGraphicComponent implements HasPosition, HasDirection, Engine, Contains<Observer<Target>>, HasPath, HasVariableSpeed {
    private         Direction direction;
    private         int life;
    private         int shield;
    private         int speed;
    protected       Path path;
    private         Timeline movementTimeline;
    private final   Set<Observer<Target>> observers = new CopyOnWriteArraySet<>();
    private         boolean hasReachedEnd = false;

    private final int maxShield;
    private final int maxLife;

    public Target(java.nio.file.Path imgPath, Scale scale, int life, int shield, int speed) {
        super(imgPath, scale, GameConstants.ANIMATION_TARGET_SPEED);

        this.maxShield = shield;
        this.maxLife = life;

        this.setLife(life);
        this.setShield(shield);
        this.setSpeed(speed);
        this.setMovementTimeline(speed);

        this.position = null;
        this.imgContainer.setVisible(false);
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

        super.start();

        if(this.position == null) {
            this.position = this.path.getStart().getPosition();
            this.direction = this.path.getStart().getDirection();
        }

        this.movementTimeline.play();
    }

    @Override
    public void stop() {
        super.stop();
        this.imgContainer.setVisible(false);
        this.movementTimeline.stop();
    }

    @Override
    public void pause() {
        super.pause();
        this.movementTimeline.pause();
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
    public Set<Observer<Target>> get() {
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
        this.life = Integer.min(this.life = Integer.max(0, life), this.maxLife);
        this.changeOpacity();
        return this;
    }

    public int getShield() {
        return this.shield;
    }

    public Target setShield(int shield) {
        this.shield = Integer.min(this.shield = Integer.max(0, shield), this.maxShield);
        this.changeOpacity();
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
        return this.getCurrentHealth() <= 0;
    }

    public void check() {
        this.observers.forEach(unit -> unit.notify(this));
    }

    public boolean hasReachedEnd() {
        return this.hasReachedEnd;
    }

    public boolean hasMoved() {
        return this.position != null;
    }

    @Override
    public void changeSpeed(double coeff) {
        super.changeAnimationSpeed(coeff);
        this.setMovementTimeline(this.speed / coeff);
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
            this.observers.forEach(observer -> observer.notify(Target.this));

            if(isDown() || this.hasReachedEnd) {
                this.stop();
            } else {
                this.direction.move(this.position);
                this.imgContainer.relocate(this.position.getX(), this.position.getY());

                if(!this.imgContainer.isVisible()) {
                    this.imgContainer.setVisible(true);
                }

                this.path.accept(Target.this);

                if (this.path.getEnd().getPosition().equals(this.position)) {
                    this.hasReachedEnd = true;
                    this.imgContainer.setVisible(false);
                }
            }
        }));

        this.movementTimeline.setCycleCount(Animation.INDEFINITE);
    }

    private void changeOpacity() {
        this.imgContainer.setOpacity((double)this.getCurrentHealth() / (double)(this.maxLife + this.maxShield));
    }

    private int getCurrentHealth() {
        return this.life + this.shield;
    }
}