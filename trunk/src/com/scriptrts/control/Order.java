package com.scriptrts.control;

import java.awt.Point;

import com.scriptrts.game.GameObject;

public abstract class Order {
    protected Point point;
    protected GameObject unit;
    
    // should be entity
    public abstract boolean isComplete();
    
    public abstract Point getPoint();
    
    public abstract boolean equals(Order o);
}
