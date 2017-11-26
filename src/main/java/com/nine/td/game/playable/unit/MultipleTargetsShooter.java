package com.nine.td.game.playable.unit;

import com.nine.td.game.path.Position;
import com.nine.td.game.playable.Target;

import java.util.List;
import java.util.stream.Collectors;

public abstract class MultipleTargetsShooter extends Unit {
    private final int maxTargets;

    public MultipleTargetsShooter(Position position, int range, int shootingRate, int power) {
        this(position, range, shootingRate, power, 0);
    }

    public MultipleTargetsShooter(Position position, int range, int shootingRate, int power, int maxTargets) {
        super(position, range, shootingRate, power);
        this.maxTargets = Integer.max(0, maxTargets);
    }

    @Override
    protected List<Target> getTargets() {
        return this.targets
                .stream()
                .filter(target -> canReach(target) && target.getPosition().distanceWith(this.getPosition()) <= this.range)
                .limit(this.maxTargets > 0 ? this.maxTargets : this.targets.size())
                .collect(Collectors.toList());
    }
}
