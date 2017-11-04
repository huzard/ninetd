package com.nine.td.game.playable;

public interface Shooter extends Observer<Target>, Contains<Target>, HasBonus {
    void shoot(Target target);
}
