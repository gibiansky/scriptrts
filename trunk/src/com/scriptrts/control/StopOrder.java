package com.scriptrts.control;

import java.util.Queue;

import com.scriptrts.game.GameObject;

/**
 * Stop all orders going on immediately after this.
 */
public class StopOrder extends Order {
    /**
     * Check for order equality.
     */
    public boolean equals(Object o) {
        return o instanceof StopOrder;
    }

    /**
     * Only complete when there are no orders
     * @return true if the unit has no orders left, false otherwise.
     */
    public boolean isComplete(GameObject unit) {
        return unit.getUnit().getOrderHandler().getOrders().size() == 0;
    }

    /**
     * Try to remove all orders from the queue
     */
    public void perform(GameObject unit, Queue<Order> orders){
        orders.clear();
        unit.getUnit().setDestination(unit.getUnit().getLocation());
    }
}
