package com.scriptrts.control;

import java.util.LinkedList;
import java.util.Queue;


import com.scriptrts.game.GameObject;

public class OrderHandler {
    /**
     * Queue of orders to execute
     */
    private Queue<Order> orders;

    /**
     * Unit on which to execute orders 
     */
    private GameObject unit;

    /**
     * Order which was last added to the queue. Used to prevent duplicates being added.
     */
    private Order lastOrder;
    
    /**
     * Create a new order handler
     * @param unit unit for which this order handler will be used
     */
    public OrderHandler(GameObject unit){
        this.unit = unit;
        orders = new LinkedList<Order>();
        lastOrder = null;
    }
    
    /**
     * Checks the current queued order whether it's complete.
     * If it isn't, keep going. If it is, get next order from queue.
     */
    public void update(){
        /* If we have an order which is finished, remove it and execute the next one */
        if(orders.peek() != null && orders.peek().isComplete(unit)){
            orders.poll();

            /* If we have another order, perform it */
            if(orders.peek() != null)
                orders.peek().perform(unit, orders);
        } 

        /* Update the order if it's not complete */
        else if(orders.peek() != null){
            orders.peek().update(unit);
        }
    }
    
    /**
     * Put an order in the queue.
     * @param order Order to queue.
     */
    public void queueOrder(Order order){
        /* If we don't have any queued orders, immediately start this one */
        if(orders.peek() == null){
            order.perform(unit, orders);
        }

        /* Check for duplicates and don't add duplicate orders */
        if(lastOrder == null || !order.equals(lastOrder)){
        	orders.add(order);
        	lastOrder = order;
        }
    }
    
    /**
     * Perform this order now. Cancels all other orders.
     * @param order Order to perform.
     */
    public void order(Order order){
        order.perform(unit, orders);
        orders.clear();
        orders.add(order);
        lastOrder = order;
    }

    /**
     * Get the list of orders for this order handler.
     * @return queue of orders to be executed
     */
    public Queue<Order> getOrders() {
        return orders;
    }

    /**
     * Get the unit that this order handler manages.
     * @return unit for which this order handler was created
     */
    public GameObject getUnit() {
        return unit;
    }
}
