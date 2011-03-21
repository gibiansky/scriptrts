package com.scriptrts.control;

import java.awt.Point;

import com.scriptrts.game.GameObject;

public class StopOrder extends Order {

    public StopOrder(GameObject unit){
        this.unit = unit;
    }
    
    @Override
    public boolean equals(Order o) {
        return o instanceof StopOrder;
    }

    @Override
    public Point getPoint() {
        return unit.getUnit().getLocation();
    }

    /**
     * Only complete when there are no orders
     */
    @Override
    public boolean isComplete() {
        return unit.getUnit().getOrderHandler().getOrders().size() == 0;
    }

}
