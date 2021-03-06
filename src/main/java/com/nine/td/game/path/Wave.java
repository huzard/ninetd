package com.nine.td.game.path;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.nine.td.GameConstants;
import com.nine.td.game.playable.*;
import com.nine.td.game.playable.Observer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Wave implements Engine, Contains<Target>, Observer<Target>, HasVariableSpeed {
    private Timeline startScheduler;

    private final Set<Target> targets = new CopyOnWriteArraySet<>();
    private final List<Runnable> onWaveUpdate = new LinkedList<>();
    private final List<Path> paths = new LinkedList<>();

    private final Deque<Target> targetsToStart;

    public Wave(List<Target> targets, List<Path> paths) {
        Preconditions.checkArgument(targets != null, "null targets");
        Preconditions.checkArgument(!targets.isEmpty(), "empty list of targets");
        Preconditions.checkArgument(paths != null, "paths null");
        Preconditions.checkArgument(paths.stream().allMatch(Path::isValid), "invalid paths definition");

        this.paths.addAll(paths.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        this.targets.addAll(targets.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        this.targetsToStart = Lists.newLinkedList(this.targets);

        this.targets.forEach(new Consumer<Target>() {
            int currentPath = 0;

            @Override
            public void accept(Target target) {
                target.setPath(paths.get(currentPath++ % paths.size()).copy());
                target.add(Wave.this);
            }
        });

        this.restartScheduler(GameConstants.START_WAVE_BASIC_SPEED);
    }

    @Override
    public void start() {
        Map<Boolean, List<Target>> hasMoved = this.targets.stream().collect(Collectors.partitioningBy(Target::hasMoved));

        if(!hasMoved.get(false).isEmpty()) {
            this.startScheduler.play();
        }

        hasMoved.get(true).forEach(Engine::start);
    }

    @Override
    public void stop() {
        this.restartScheduler(GameConstants.START_WAVE_BASIC_SPEED);
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
    public Set<Target> get() {
        return Collections.unmodifiableSet(this.targets);
    }

    @Override
    public void notify(Target data) {
        if(this.targets.contains(data) && (data.isDown() || data.hasReachedEnd())) {
            this.remove(data);
            this.onWaveUpdate.forEach(Runnable::run);
        }
    }

    @Override
    public void changeSpeed(double coeff) {
        this.targets.forEach(target -> target.changeSpeed(coeff));

        this.restartScheduler(GameConstants.START_WAVE_BASIC_SPEED / coeff);
        this.startScheduler.play();
    }

    public void onWaveUpdate(Runnable runnable) {
        if(runnable != null) {
            this.onWaveUpdate.add(runnable);
        }
    }

    public List<Path> getPaths() {
        return Collections.unmodifiableList(this.paths);
    }

    private void restartScheduler(double time) {
        if(this.startScheduler != null) {
            this.startScheduler.stop();
        }

        this.startScheduler = new Timeline(
                new KeyFrame(Duration.ZERO, event -> {
                    if(this.targetsToStart.isEmpty()) {
                        this.startScheduler.stop();
                    } else {
                        this.paths.forEach(path -> Optional.ofNullable(this.targetsToStart.poll()).ifPresent(Engine::start));
                    }
                }), new KeyFrame(Duration.millis(time)));

        this.startScheduler.setCycleCount(Animation.INDEFINITE);
    }
}
