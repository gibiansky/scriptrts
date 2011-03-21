package com.scriptrts.control;

import java.awt.Point;

import com.scriptrts.game.GameObject;

public class FollowOrder extends Order {

    GameObject unit;
    GameObject target;
    
    public FollowOrder(GameObject unit, GameObject target){
        this.unit = unit;
        this.target = target;
    }
    
    public GameObject getUnit() {
        return unit;
    }

    public GameObject getTarget() {
        return target;
    }

    @Override
    public boolean equals(Order o) {
        return o instanceof FollowOrder && target == ((FollowOrder)o).target;
    }

    @Override
    public Point getPoint() {
        return new Point(target.getUnit().getX(), target.getUnit().getY());
    }

    @Override
    public boolean isComplete() {
        return !target.getUnit().isAlive() || unit.equals(target);
    }


}
