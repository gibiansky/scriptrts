package com.scriptrts.control;

import java.awt.Point;

import com.scriptrts.game.SimpleUnit;

public class FollowOrder extends Order {

    SimpleUnit unit;
    SimpleUnit target;
    
    public FollowOrder(SimpleUnit unit, SimpleUnit target){
        this.unit = unit;
        this.target = target;
    }
    
    public SimpleUnit getUnit() {
        return unit;
    }

    public SimpleUnit getTarget() {
        return target;
    }

    @Override
    public boolean equals(Order o) {
        return o instanceof FollowOrder && target == ((FollowOrder)o).target;
    }

    @Override
    public Point getPoint() {
        return new Point(target.getX(), target.getY());
    }

    @Override
    public boolean isComplete() {
        return !target.isAlive() || unit.equals(target);
    }


}
