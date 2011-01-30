package com.scriptrts.core;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;

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
    private Polygon limit;

    /**
     * Map limits used to determine which quad we're in 
     */
    private int mapX, mapY;

    /**
     * Default constructor
     */
    public Viewport(){
    	width = 0;
    	height = 0;
    	mapX = 0;
    	mapY = 0;
    }
    
    /**
     * Create a new viewport
     */
    public Viewport(int w, int h, int mapSizeX, int mapSizeY){
        super();

        width = w;
        height = h;
        mapX = mapSizeX;
        mapY = mapSizeY;
    }
    
    /**
     * Set dimensions
     */
    public void setDim(int w, int h){
    	width = w;
    	height = h;
    }
    
    /**
     * Set map size 
     */
    public void setMapSize(int mapSizeX, int mapSizeY){
        mapX = mapSizeX;
        mapY = mapSizeY;
    }

    /**
     * Move the viewport by the specified deltas
     */
    public void translate(int deltaX, int deltaY){

        int origY = y;
        int origX = x;
        x += deltaX;
        y += deltaY;

        boolean movingX = Math.abs(deltaX) > Math.abs(deltaY);

        /* Prevent viewport from moving outside specified range */
        if(viewportMotionLimited && !limit.contains(x, y)){
            /* If this is due to being in a corner, just don't apply the movement */
            for(int i = 0; i < limit.npoints; i++){
                int threshold = 10;
                if(Point2D.distanceSq(x, y, limit.xpoints[i], limit.ypoints[i]) <= threshold * threshold){
                    x -= deltaX;
                    y -= deltaY;
                    return;
                }
            }

            /* If we're not in a corner, do sliding movements */
            int increments = 0;
            while(!limit.contains(x, y)){
                increments++;

                /* No infinite looping */
                if(increments >= 200){
                    x = origX;
                    y = origY;
                    return;
                }

                /* Top */
                if(origY < mapY / 2){
                    /* Left */
                    if(origX < mapX / 2){
                        if(movingX) y++;
                        else x++;
                    }
                    /* Right */
                    else {
                        if(movingX) y++;
                        else x--;
                    }
                }

                /* Bottom */
                else {
                    /* Left */
                    if(origX < mapX / 2){
                        if(movingX) y--;
                        else x++;
                    }
                    /* Right */
                    else {
                        if(movingX) y--;
                        else x--;
                    }
                }
            }
        }

    }

    /**
     * Set viewport motion limits
     */
    public void setViewportLocationLimits(Polygon limit){
        viewportMotionLimited = true;
        this.limit = limit;
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
