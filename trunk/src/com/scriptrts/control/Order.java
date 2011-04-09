package com.scriptrts.control;

import java.util.Queue;

import com.scriptrts.game.GameObject;

/**
 * Generic order that can be given to a unit.
 */
public abstract class Order {
    /**
     * Return whether this order has been completed.
     * @return true if the order is finished and can be removed, false otherwise.
     */
    public abstract boolean isComplete(GameObject unit);
    
    /**
     * Check whether this order is equivalent to another.
     * @return true if the orders are equivalent, false otherwise;
     */
    public abstract boolean equals(Order o);

    /**
     * Start performing the order.
     * @param unit unit which is being ordered
     * @param orders other orders which the unit will implement afterwards
     */
    public abstract void perform(GameObject unit, Queue<Order> orders);

    /**
     * Update an order with new information.
     * @param unit unit which this order is controlling.
     */
    public void update(GameObject unit) {}
}
