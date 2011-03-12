package com.scriptrts.control;

import java.awt.Point;

import com.scriptrts.game.Entity;
import com.scriptrts.game.SimpleUnit;

public abstract class Order {
    protected Point point;
    
    // should be entity
    public abstract boolean isComplete(SimpleUnit e);
    
    public abstract Point getPoint();
    
}
