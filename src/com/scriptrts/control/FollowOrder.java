package com.scriptrts.control;

import java.awt.Point;
import java.util.Queue;

import com.scriptrts.game.GameObject;

/**
 * Order a unit to follow another unit.
 */
public class FollowOrder extends Order {

    /**
     * Unit to follow.
     */
    private GameObject target;
    
    /**
     * Counter to determine when to update calls
     */
    private int count;
    
    /**
     * Number of frames between updates to the follow order
     */
    private int updateFrequency = 20;
    
    /**
     * Create a new follow order to follow a given unit.
     * @param target unit to follow
     */
    public FollowOrder(GameObject target){
        super();
        this.target = target;
    }
    
    /**
     * Check whether two orders are equal
     */
    public boolean equals(Order o) {
        return o instanceof FollowOrder && target == ((FollowOrder)o).target;
    }

    /**
     * Check whether this order is complete.
     */
    public boolean isComplete(GameObject unit) {
        return !target.getUnit().isAlive() || unit.equals(target);
    }

    /**
     * Start performing the order.
     */
    public void perform(GameObject unit, Queue<Order> orders){
        Point point = new Point(target.getUnit().getX(), target.getUnit().getY());
        unit.getUnit().setDestination(point);
    }

    /**
     * Update the location to which this unit is moving.
     */
    public void update(GameObject unit){
    	if(count == updateFrequency)
    		perform(unit, null);
    	count++;
    	if(count > updateFrequency)
    		count = 0;
    }
}
