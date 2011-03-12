package com.scriptrts.control;

import java.awt.Point;

import com.scriptrts.game.Entity;

public class MoveOrder extends Order {
    private Point point;
    
    public MoveOrder(Point point){
        this.point = point;
    }
   
    public Point getPoint(){
        return point;
    }
    

    /**
     * Assumes e is the unit being moved.
     * @return true if e is in final position of order, else false.
     * @param e unit being moved.
     */
    @Override
    public boolean complete(Entity e) {
        return e.getPoint() == point;
    }
}
