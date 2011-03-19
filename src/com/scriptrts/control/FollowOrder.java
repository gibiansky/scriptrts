package com.scriptrts.control;

import java.awt.Point;

import com.scriptrts.game.Unit;

public class FollowOrder extends Order {
    private Unit target;
    
    public FollowOrder(Unit unit, Unit target){
        this.unit = unit;
        this.target = target;
    }
    


    public Unit getTarget() {
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
