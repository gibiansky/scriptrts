package com.scriptrts.control;

import java.awt.Point;

import com.scriptrts.game.Entity;
import com.scriptrts.game.SimpleUnit;

public abstract class Order {
    protected Point point;
    protected SimpleUnit unit;
    
    // should be entity
    public abstract boolean isComplete();
    
    public abstract Point getPoint();
    
    public abstract boolean equals(Order o);
}
