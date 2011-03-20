package com.scriptrts.control;

import java.awt.Point;

import com.scriptrts.game.Unit;

public class MoveOrder extends Order implements Comparable<MoveOrder> {
   
    
    public MoveOrder(Point point, Unit unit){
        this.point = point;
        this.unit = unit;
    }
   
    public Point getPoint(){
        return point;
    }
    
    @Override
    public boolean equals(Object other){
        if(! (other instanceof MoveOrder))
            return false;
        else
            return this.point.x == ((MoveOrder)other).point.x && this.point.y == ((MoveOrder)other).point.y;
    }

    /**
     * Assumes e is the unit being moved.
     * @return true if e is in final position of order, else false.
     * @param e unit being moved.
     */
    @Override
    public boolean isComplete() {
        return unit.getX() == point.getX() && unit.getY() == point.getY();
    }

    /**
     * Sorts in order of distance. Note that distance isn't actually distance but that squared.
     * Since square root is monotonic, this don't matter.
     */
    @Override
    public int compareTo(MoveOrder other) {
        double distance = Math.pow(this.point.x - other.point.x,2) + Math.pow(this.point.y - other.point.y,2);
        return (int)distance == 0 ? 0 : 1 + (int)distance;
    }

	@Override
	public boolean equals(Order o) {
		return (this.point.x == o.getPoint().x) && (this.point.y == o.getPoint().y);
	}
}
