package com.scriptrts.game;

public abstract class Entity {
    // Location of top left corner
    private int x;
    private int y;
    // Size
    private int height;
    private int width;
    
    /**
     * Determines whether this is passable or transparent to c.
     * Specific implementation depends on Construct type.
     * @param c Construct attempting to determine whether this is passable.
     * @return true if this is passable, false otherwise.
     */
    public abstract boolean isPassable(Construct c);
   
}
