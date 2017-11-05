package com.nine.td.game.path;

import com.google.common.base.Preconditions;
import com.nine.td.game.playable.Contains;
import com.nine.td.game.playable.Engine;
import com.nine.td.game.playable.Observer;
import com.nine.td.game.playable.Target;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Wave implements Engine, Contains<Target>, Observer<Target> {
    private final List<Target> targets = new CopyOnWriteArrayList<>();

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
    }

    @Override
    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(this.targets.size());

        this.targets.forEach(new Consumer<Target>() {
            long current = 0L;

            @Override
            public void accept(Target target) {
                scheduler.schedule(target::start, (current = current + 500L), TimeUnit.MILLISECONDS);
            }
        });
    }

    @Override
    public void stop() {
        this.targets.forEach(Engine::stop);
    }

    @Override
    public void pause() {
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

    public boolean isEnded() {
        return this.targets.isEmpty() || this.targets.stream().allMatch(Target::hasReachedEnd);
    }
}
