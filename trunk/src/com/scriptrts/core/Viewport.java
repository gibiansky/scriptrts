package com.scriptrts.core;

import java.awt.Point;

public class Viewport {
    /**
     * Dimensions of the viewport
     */
    private int width, height;

    /**
     * Location of the top left corner of the viewport
     */
    private int x, y;

    /**
     * Whether viewport motion is limited
     */
    private boolean viewportMotionLimited = false;

    /**
     * Limits on the area that the viewport can be looking at
     */
    private int lowLimitX, lowLimitY, highLimitX, highLimitY;

    /**
     * Create a new viewport
     */
    public Viewport(int w, int h){
        super();

        width = w;
        height = h;
    }

    /**
     * Move the viewport by the specified deltas
     */
    public void translate(int deltaX, int deltaY){
        x += deltaX;
        y += deltaY;

        /* Prevent viewport from moving outside specified range */
        if(viewportMotionLimited){
            if(x > highLimitX - width) x = highLimitX - width;
            if(x < lowLimitX) x = lowLimitX;
            if(y > highLimitY - height) y = highLimitY - height;
            if(y < lowLimitY) y = lowLimitY;
        }
    }

    /**
     * Set viewport motion limits
     */
    public void setViewportLocationLimits(int xLow, int yLow, int xHigh, int yHigh){
        viewportMotionLimited = true;
        lowLimitX = xLow;
        lowLimitY = yLow;
        highLimitX = xHigh;
        highLimitY = yHigh;
    }

    /**
     * Resize viewport
     */
    public void resize(int newWidth, int newHeight){
        width = newWidth;
        height = newHeight;
    }

    /**
     * Get height of the viewport
     */
    public int getHeight(){
        return height;
    }

    /**
     * Get width of the viewport 
     */
    public int getWidth(){
        return width;
    }

    /**
     * Get location of the viewport
     */
    public Point getLocation(){
        return new Point(x, y);
    }

    /**
     * Get the x component of the location
     */
    public int getX(){
        return x;
    }

    /**
     * Get the y component of the location
     */
    public int getY(){
        return y;
    }

    /**
     * Set viewport location
     */
    public void setLocation(Point p){
        x = (int) p.x;
        y = (int) p.y;
    }

    /**
     * Set viewport location
     */
    public void setLocation(int x, int y){
        this.x = x;
        this.y = y;
    }

    /**
     * Get printable version of a viewport
     */
    public String toString(){
        return "(Viewport at (" + x + ", " + y + ") sized " + width + "x" + height + ")";
    }
}
