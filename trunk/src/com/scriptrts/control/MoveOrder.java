package com.scriptrts.control;

import java.awt.Point;
import java.util.Queue;

import com.scriptrts.game.GameObject;

/**
 * Order a unit to move to a given location
 */
public class MoveOrder extends Order implements Comparable<MoveOrder> {
    /**
     * Where this unit should move.
     */
    private Point point;

    /**
     * Create a new move order
     */
    public MoveOrder(Point point){
        this.point = point;
    }

    /**
     * Check whether this order is equivalent to another.
     */
    public boolean equals(Order other){
        if(! (other instanceof MoveOrder))
            return false;
        else
            return point.x == ((MoveOrder)other).point.x && point.y == ((MoveOrder)other).point.y;
    }

    /**
     * Checks if this unit has finished moving to its desired location.
     * @return true if the unit is in final position of order, else false.
     */
    public boolean isComplete(GameObject unit) {
        return unit.getUnit().getX() == point.getX() && unit.getUnit().getY() == point.getY();
    }

    /**
     * Sorts in order of distance. Note that distance isn't actually distance but that squared.
     * Since square root is monotonic, this doesn't matter.
     */
    public int compareTo(MoveOrder other) {
        double distance = Math.pow(this.point.x - other.point.x,2) + Math.pow(this.point.y - other.point.y,2);
        return (int)distance == 0 ? 0 : 1 + (int)distance;
    }

    /**
     * Start performing this order.
     */
    public void perform(GameObject unit, Queue<Order> orders){
        unit.getUnit().setDestination(point);
    }

    public Point getPoint(){
        return point;
    }
}
