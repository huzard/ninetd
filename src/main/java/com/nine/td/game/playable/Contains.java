package com.nine.td.game.playable;

import java.util.List;

public interface Contains<P> {
    boolean add(P data);
    boolean remove(P data);
    List<P> get();
}
