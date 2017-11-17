package com.nine.td.game.playable;

import java.util.Set;

public interface Contains<P> {
    boolean add(P data);
    boolean remove(P data);
    Set<P> get();
}
