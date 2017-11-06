package com.nine.td.game.playable.unit;

import com.google.common.base.Preconditions;
import com.nine.td.game.path.HasPosition;
import com.nine.td.game.path.Position;
import com.nine.td.game.playable.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Représente une unité de combat
 */
public abstract class Unit implements HasPosition, Shooter, HasVariableSpeed, Engine {
    private static final long MAX_DELAY = 1400L;

    protected Position position;

    private final List<Target> targets = new ArrayList<>();
    private int range;
    private double shootingRate;
    private int power;
    private Timeline shootingTimer;

    public Unit(Position position, int range, double shootingRate, int power) {
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
        return target != null && target.add(this) && this.targets.add(target);
    }

    @Override
    public boolean remove(Target target) {
        return target != null && target.remove(this) && this.targets.remove(target);
    }

    @Override
    public List<Target> get() {
        return targets;
    }

    @Override
    public void notify(Target data) {
        if(data.isDown() || (this.targets.contains(data) && !canReach(data))) {
            remove(data);
        }
    }

    @Override
    public void consume(Bonus bonus) {
        bonus.applyBonus(this);
    }

    @Override
    public void start() {
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

    /**
     * Récupère le temps d'attente entre deux exécution d'actions
     */
    private long delay() {
        return MAX_DELAY - (long) (this.shootingRate * 1000);
    }

    private boolean canReach(Target target) {
        return this.position.distanceWith(target.getPosition()) <= this.range;
    }

    public int getRange() {
        return this.range;
    }

    public Unit setRange(int range) {
        this.range = Integer.max(0, range);
        return this;
    }

    public int getPower() {
        return this.power;
    }

    public Unit setPower(int power) {
        this.power = Integer.max(0, power);
        return this;
    }

    @Override
    public void changeSpeed(double coeff) {
        this.setTimeline(this.shootingRate / coeff);
        start();
    }

    private void setTimeline(double speed) {
        Preconditions.checkArgument(speed > 0, "require positive speed");

        if(this.shootingTimer != null) {
            this.shootingTimer.stop();
        }

        this.shootingTimer = new Timeline(new KeyFrame(Duration.millis(this.delay()), irrelevant -> getNearestTarget().ifPresent(target -> {
            shoot(target);
            target.check();
        })));

        this.shootingTimer.setCycleCount(Animation.INDEFINITE);
    }
}
