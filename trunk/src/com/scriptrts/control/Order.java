package com.scriptrts.control;

import java.awt.Point;

import com.scriptrts.game.Unit;

public abstract class Order {
    protected Point point;
    protected Unit unit;
    
    // should be entity
    public abstract boolean isComplete();
    
    public abstract Point getPoint();
    
    public abstract boolean equals(Order o);
    
    public Unit getUnit() {
        return unit;
    }
}
