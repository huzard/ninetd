package com.nine.td.game.playable.unit;

import com.nine.td.game.path.HasPosition;
import com.nine.td.game.path.Position;
import com.nine.td.game.playable.Bonus;
import com.nine.td.game.playable.Engine;
import com.nine.td.game.playable.Shooter;
import com.nine.td.game.playable.Target;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.*;

/**
 * Représente une unité de combat
 */
public abstract class Unit implements HasPosition, Shooter, Engine {
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
    public void check(Target target) {
        if(target.isDown() || (this.targets.contains(target) && !canReach(target))) {
            remove(target);
        }
    }

    @Override
    public void consume(Bonus bonus) {
        bonus.applyBonus(this);
    }

    @Override
    public void start() {
        if(this.shootingTimer == null) {
            this.shootingTimer = new Timeline(new KeyFrame(Duration.millis(this.delay()), irrelevant -> getNearestTarget().ifPresent(target -> {
                shoot(target);
                target.check();
            })));

            this.shootingTimer.setCycleCount(Animation.INDEFINITE);
            this.shootingTimer.play();
        }
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
        return MAX_DELAY - (long) (this.getShootingRate() * 1000);
    }

    private boolean canReach(Target target) {
        return this.position.distanceWith(target.getPosition()) <= this.range;
    }

    private double getShootingRate() {
        return Double.max(this.shootingRate, 1.0);
    }

    public Unit setShootingRate(double shootingRate) {
        this.shootingRate = Double.max(shootingRate, 1.0);
        return this;
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
}
