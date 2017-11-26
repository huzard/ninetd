package com.nine.td.game.playable.unit;

import com.nine.td.game.path.Position;

public abstract class SingleTargetShooter extends MultipleTargetsShooter {
    public SingleTargetShooter(Position position, int range, int shootingRate, int power) {
        super(position, range, shootingRate, power, 1);
    }
}
