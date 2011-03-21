package com.scriptrts.control;

import java.util.LinkedList;
import java.util.Queue;


import com.scriptrts.game.GameObject;

public class OrderHandler {
    Queue<Order> orders;
    GameObject unit;
    Order lastOrder;
    
    public OrderHandler(GameObject unit){
        this.unit = unit;
        orders = new LinkedList<Order>();
        lastOrder = null;
    }
    
    /**
     * The method that actually handles the order
     * @param order the order to handle
     */
    public void handleOrder(Order order){
        if(order instanceof MoveOrder)
            unit.getUnit().setDestination(((MoveOrder) order).getPoint());
        if(order instanceof FollowOrder)
            unit.getUnit().setDestination(((FollowOrder) order).getPoint());
        if(order instanceof StopOrder){
            orders.clear();
            unit.getUnit().setDestination(unit.getUnit().getLocation());
        }
    }
    
    /**
     * Checks the current queued order whether it's complete.
     * If it isn't, keep going. If it is, get next order from queue.
     */
    public void update(){
        if(orders.peek() instanceof MoveOrder || orders.peek() instanceof FollowOrder)
            if(orders.peek().isComplete()){
                orders.poll();
                this.handleOrder(orders.peek());
            } else if(orders.peek() instanceof FollowOrder)
                handleOrder(orders.peek());
        if(orders.peek() instanceof StopOrder)
            handleOrder(orders.peek());
            
    }
    
    /**
     * Put an order in the queue.
     * @param order Order to queue.
     */
    public void queueOrder(Order order){
        if(orders.peek() == null){
            handleOrder(order);
        }
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
        handleOrder(order);
        orders.clear();
        orders.add(order);
        lastOrder = order;
    }

    public Queue<Order> getOrders() {
        return orders;
    }

    public GameObject getUnit() {
        return unit;
    }
}
