package com.scriptrts.core;

import java.awt.Graphics;

/**
 * Action that should be taken when the map is next clicked
 */
public abstract class ClickAction {
    /**
     * Method called when map is clicked
     * @param x x coordinate on map
     * @param y y coordinate on map
     */
    public abstract void click(int x, int y);

    /**
     * Draws cursor for the action
     * @param graphics graphics handle on the screen
     */
    public void paintCursor(Graphics graphics){}
    
    /**
     * Does this action have a custom cursor?
     * @return hasCursor whether this action has a custom cursor
     */
    public boolean hasCursor(){return false;}
}
