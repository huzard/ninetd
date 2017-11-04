package com.nine.td.game.path;

import com.google.common.base.Preconditions;
import com.nine.td.game.playable.Target;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Path implements Consumer<Target> {
    private WayPoint start;
    private List<WayPoint> wayPoints;
    private WayPoint end;

    public Path(List<WayPoint> wayPoints) {
        Preconditions.checkState(wayPoints != null, "null path");
        Preconditions.checkState(wayPoints.size() >= 2, "require start/way/end points");

        this.start = wayPoints.get(0);
        this.wayPoints = wayPoints.size() > 2 ? wayPoints.subList(1, wayPoints.size() - 1) : Collections.emptyList();
        this.end = wayPoints.get(wayPoints.size() - 1);
    }

    @Override
    public void accept(Target target) {
        this.wayPoints
                .stream()
                .filter(wayPoint -> wayPoint.getPosition().equals(target.getPosition()))
                .findFirst()
                .ifPresent(wayPoint -> target.setDirection(wayPoint.getDirection()));
    }

    public WayPoint getStart() {
        return this.start;
    }

    public List<WayPoint> getWayPoints() {
        return this.wayPoints;
    }

    public WayPoint getEnd() {
        return this.end;
    }

    public boolean isValid() {
        return this.start != null && this.wayPoints != null && !this.wayPoints.isEmpty();
    }

    //copy
    public Path copy() {
        List<WayPoint> toCopy = new LinkedList<>();
        toCopy.add(this.start);
        toCopy.addAll(this.wayPoints);
        toCopy.add(this.end);

        return new Path(toCopy.stream().map(wayPoint -> new WayPoint(wayPoint.getDirection(), new Position(wayPoint.getPosition().getX(), wayPoint.getPosition().getY()))).collect(Collectors.toList()));
    }
}
