package com.scriptrts.combat;

import com.scriptrts.game.Construct;
import com.scriptrts.game.Sprite;

public abstract class Unit extends Construct {
    protected double movementSpeed;
    protected double attackSpeed;
    // This is an int array because we may want different types of attack. 
    //ex.: [land_attack air_attack] or [attack1 attack2] or something depending on plot
    protected int[][] damage;
    
    protected Sprite sprite;
    
    // For weird-behaving units
    protected boolean water;
    protected boolean air;
    
    protected Direction direction;

    protected Action curAction;
    
    protected enum Direction{N, NE, E, SE, S, SW, W, NW};

    public abstract boolean act(Action a);
    
    public double getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(double movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public double getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(double attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public int[][] getDamage() {
        return damage;
    }

    public void setDamage(int[][] damage) {
        this.damage = damage;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public boolean isWater() {
        return water;
    }

    public void setWater(boolean water) {
        this.water = water;
    }

    public boolean isAir() {
        return air;
    }

    public void setAir(boolean air) {
        this.air = air;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    };

}
