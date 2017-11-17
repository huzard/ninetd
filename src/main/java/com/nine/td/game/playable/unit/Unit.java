package com.nine.td.game.playable.unit;

import com.google.common.base.Preconditions;
import com.nine.td.game.path.HasPosition;
import com.nine.td.game.path.Position;
import com.nine.td.game.playable.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Représente une unité de combat
 */
public abstract class Unit implements HasPosition, Shooter, HasVariableSpeed, Engine {
    private static final long MAX_DELAY = 10000;

    protected Position position;

    private final Set<Target> targets = new HashSet<>();
    protected int range;
    protected int shootingRate;
    protected int power;
    private Timeline shootingTimer;

    public Unit(Position position, int range, int shootingRate, int power) {
        this.position = position;
        this.range = range;
        this.shootingRate = shootingRate;
        this.power = power;
        this.setTimeline(this.shootingRate);
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
    public boolean add(Target target) {
        return this.targets.add(target);
    }

    @Override
    public boolean remove(Target target) {
        return target != null && target.remove(this) && this.targets.remove(target);
    }

    @Override
    public Set<Target> get() {
        return targets;
    }

    @Override
    public void notify(Target data) {
        if(data != null) {
            if (!this.targets.contains(data) && canReach(data) && !data.isDown()) {
                add(data);
            } else if (this.targets.contains(data) && (!canReach(data) || data.isDown())) {
                remove(data);
            }
        }
    }

    @Override
    public void consume(Bonus bonus) {
        bonus.applyBonus(this);
    }

    @Override
    public void start() {
        stop();
        this.shootingTimer.play();
    }

    @Override
    public void stop() {
        if(this.shootingTimer != null) {
            this.shootingTimer.stop();
        }
    }

    @Override
    public void pause() {
        this.shootingTimer.pause();
    }

    @Override
    public void changeSpeed(double coeff) {
        double speed = this.shootingRate / coeff;
        System.out.println(speed);
        this.setTimeline(MAX_DELAY - (speed > MAX_DELAY ? MAX_DELAY - 1 : speed));
        start();
    }

    private void setTimeline(double speed) {
        Preconditions.checkArgument(speed > 0, "require positive speed (found " + speed + ")");

        if(this.shootingTimer != null) {
            this.shootingTimer.stop();
        }

        this.shootingTimer = new Timeline(new KeyFrame(Duration.millis(speed), irrelevant -> getNearestTarget().ifPresent(target -> {
            shoot(target);
            target.check();
        })));

        this.shootingTimer.setCycleCount(Animation.INDEFINITE);
    }

    /**
     * Récupère la cible la plus proche et purge la liste des cibles trop loin
     */
    private Optional<Target> getNearestTarget() {
        //On clean
        this.targets.removeIf(target -> !canReach(target));

        //On cherche la cible avec la distance la plus courte
        return this.targets
                .stream()
                .min((t1, t2) -> Double.compare(t1.getPosition().distanceWith(this.getPosition()), t2.getPosition().distanceWith(this.position)));
    }

    private boolean canReach(Target target) {
        return this.position.distanceWith(target.getPosition()) <= this.range;
    }

    public int getRange() {
        return range;
    }
}
