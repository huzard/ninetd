package com.nine.td.game.path;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.nine.td.game.playable.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Wave implements Engine, Contains<Target>, Observer<Target>, HasVariableSpeed {
    private final List<Target> targets = new CopyOnWriteArrayList<>();
    private Timeline startScheduler;

    public Wave(List<Target> targets, List<Path> paths) {
        Preconditions.checkArgument(targets != null, "null targets");
        Preconditions.checkArgument(!targets.isEmpty(), "empty list of targets");
        Preconditions.checkArgument(paths != null, "paths null");
        Preconditions.checkArgument(paths.stream().allMatch(Path::isValid), "invalid paths definition");

        this.targets.addAll(targets.stream().filter(Objects::nonNull).collect(Collectors.toList()));

        this.targets.forEach(new Consumer<Target>() {
            int currentPath = 0;

            @Override
            public void accept(Target target) {
                target.setPath(paths.get(currentPath++ % paths.size()).copy());
                target.add(Wave.this);
            }
        });

        this.restartScheduler();
    }

    private void restartScheduler() {
        if(this.startScheduler != null) {
            this.startScheduler.stop();
        }

        Deque<Target> targetsToStart = Lists.newLinkedList(this.targets);

        this.startScheduler = new Timeline(new KeyFrame(Duration.millis(500L), event -> targetsToStart.poll().start()));
        this.startScheduler.setCycleCount(targetsToStart.size());
    }

    @Override
    public void start() {
        Map<Boolean, List<Target>> hasMoved =  this.targets.stream().collect(Collectors.partitioningBy(Target::hasMoved));

        if(!hasMoved.get(false).isEmpty()) {
            this.startScheduler.play();
        }

        hasMoved.get(true).forEach(Engine::start);
    }

    @Override
    public void stop() {
        this.restartScheduler();
        this.targets.forEach(Engine::stop);
    }

    @Override
    public void pause() {
        this.startScheduler.pause();
        this.targets.forEach(Engine::pause);
    }

    @Override
    public boolean add(Target target) {
        return this.targets.add(target);
    }

    @Override
    public boolean remove(Target target) {
        return this.targets.remove(target);
    }

    @Override
    public List<Target> get() {
        return this.targets;
    }

    @Override
    public void check(Target target) {
        if(this.targets.contains(target) && (target.isDown() || target.hasReachedEnd())) {
            remove(target);
        }
    }

    @Override
    public void changeSpeed(double coeff) {
        this.targets.forEach(target -> target.changeSpeed(coeff));
    }
}
