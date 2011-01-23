package com.scriptrts.game;

public abstract class Entity {
    // Location of top left corner
    private int x;
    private int y;
    // Size
    private int height;
    private int width;
    
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Determines whether this is passable or transparent to c.
     * Specific implementation depends on Construct type.
     * @param c Construct attempting to determine whether this is passable.
     * @return true if this is passable, false otherwise.
     */
    public abstract boolean isPassable(Construct c);
   
}
