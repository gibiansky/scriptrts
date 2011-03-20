package com.scriptrts.control;

import java.awt.Point;

import com.scriptrts.game.Unit;

public class StopOrder extends Order {

    public StopOrder(Unit unit){
        this.unit = unit;
    }
    
    @Override
    public boolean equals(Order o) {
        return o instanceof StopOrder;
    }

    @Override
    public Point getPoint() {
        return unit.getLocation();
    }

    /**
     * Only complete when there are no orders
     */
    @Override
    public boolean isComplete() {
        return unit.getOrderHandler().getOrders().size() == 0;
    }

}
