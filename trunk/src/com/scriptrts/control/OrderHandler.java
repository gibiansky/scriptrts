package com.scriptrts.control;

import java.util.LinkedList;
import java.util.Queue;

import com.scriptrts.combat.Unit;
import com.scriptrts.game.SimpleUnit;

public class OrderHandler {
    Queue<Order> orders;
    SimpleUnit unit;
    
    public OrderHandler(SimpleUnit unit){
        this.unit = unit;
        orders = new LinkedList<Order>();
    }
    
    /**
     * The method that actually handles the order
     * @param order the order to handle
     */
    public void handleOrder(Order order){
        if(order instanceof MoveOrder)
            unit.setDestination(((MoveOrder) order).getPoint());
    }
    
    /**
     * Checks the current queued order whether it's complete.
     * If it isn't, keep going. If it is, get next order from queue.
     */
    public void update(){
        if(orders.peek() instanceof MoveOrder)
            if(orders.peek().isComplete(unit)){
                orders.poll();
                this.handleOrder(orders.peek());
            }
            
    }
    
    /**
     * Put an order in the queue.
     * @param order Order to queue.
     */
    public void queueOrder(Order order){
        if(orders.peek() == null){
            handleOrder(order);
        }
        orders.add(order);
    }
    
    /**
     * Perform this order now. Cancels all other orders.
     * @param order Order to perform.
     */
    public void order(Order order){
        handleOrder(order);
        orders.clear();
        orders.add(order);
    }

    public Queue<Order> getOrders() {
        return orders;
    }

    public SimpleUnit getUnit() {
        return unit;
    }
}
