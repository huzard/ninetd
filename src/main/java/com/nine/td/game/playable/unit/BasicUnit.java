package com.nine.td.game.playable.unit;

import com.nine.td.game.path.Position;
import com.nine.td.game.playable.Target;

/**
 * Implémentation basique d'une unitée
 */
public class BasicUnit extends Unit {
    public BasicUnit(Position position) {
        super(position, 10, 0.2, 5);
    }

    @Override
    public void shoot(Target target) {
        System.out.println(this + " : shooting target");

        if(target.getShield() > 0) {
            int difference = target.getShield() - this.getPower();

            if(difference > target.getShield()) {
                int remain = difference - target.getShield();
                target.setShield(0).setLife(target.getLife() - remain);
            } else {
                target.setShield(difference);
            }
        } else {
            target.setLife(target.getLife() - this.getPower());
        }
    }
}
