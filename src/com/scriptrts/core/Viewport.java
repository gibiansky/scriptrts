package com.scriptrts.core;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;

/**
 * Viewport defining how the user is looking at the game map.
 */
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
     * @param w width of viewport
     * @param h height of viewport
     * @param mapSizeX horizontal size of map in pixels
     * @param mapSizeY vertical size of map in pixels
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
     * @param w width of viewport
     * @param h height of viewport
     */
    public void setDim(int w, int h){
    	width = w;
    	height = h;
    }
    
    /**
     * Set map size 
     * @param mapSizeX horizontal size of map in pixels
     * @param mapSizeY vertical size of map in pixels
     */
    public void setMapSize(int mapSizeX, int mapSizeY){
        mapX = mapSizeX;
        mapY = mapSizeY;
    }

    /**
     * Move the viewport by the specified deltas
     * @param deltaX how much to shift in the x direction
     * @param deltaY how much to shift in the y direction
     */
    public void translate(int deltaX, int deltaY){
        int origY = y;
        int origX = x;
        x += deltaX;
        y += deltaY;

        if(!limit.contains(origX, origY))
        	return;
        
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
                if(increments >= 100){
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
     * @param limit Polygon defining where the viewport top left corner is allowed to be
     */
    public void setViewportLocationLimits(Polygon limit){
        viewportMotionLimited = true;
        this.limit = limit;
    }

    /**
     * Resize viewport
     * @param newWidth desired new width of viewport
     * @param newHeight desired new height of viewport
     */
    public void resize(int newWidth, int newHeight){
        width = newWidth;
        height = newHeight;
    }

    /**
     * Get height of the viewport
     * @return viewport height
     */
    public int getHeight(){
        return height;
    }

    /**
     * Get width of the viewport 
     * @return viewport width
     */
    public int getWidth(){
        return width;
    }

    /**
     * Get location of the viewport
     * @return location of the viewport
     */
    public Point getLocation(){
        return new Point(x, y);
    }

    /**
     * Get the x component of the location
     * @return x coordinate of viewport location
     */
    public int getX(){
        return x;
    }

    /**
     * Get the y component of the location
     * @return y coordinate of viewport location
     */
    public int getY(){
        return y;
    }

    /**
     * Set viewport location
     * @param p new viewport location
     */
    public void setLocation(Point p){
        x = (int) p.x;
        y = (int) p.y;
    }

    /**
     * Set viewport location
     * @param x new x coordinate of location
     * @param y new y coordinate of location
     */
    public void setLocation(int x, int y){
        this.x = x;
        this.y = y;
    }
}
