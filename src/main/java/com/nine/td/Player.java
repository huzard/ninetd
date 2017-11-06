package com.nine.td;

public class Player {
    private int health;
    private int score;
    private int money;

    public Player(int health, int score, int money) {
        this.health = health;
        this.score = score;
        this.money = money;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }
}
