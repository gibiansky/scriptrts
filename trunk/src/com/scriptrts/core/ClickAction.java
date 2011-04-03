package com.scriptrts.core;

import java.awt.Graphics;

import com.scriptrts.core.ui.Viewport;

/**
 * Action that should be taken when the map is next clicked
 */
public abstract class ClickAction {
    /**
     * Method called when map is clicked
     * @param x x coordinate of click
     * @param y y coordinate of click
     */
    public abstract void click(int x, int y);

    /**
     * Draws cursor for the action
     * @param graphics graphics handle on the screen
     */
    public void paintCursor(Graphics graphics, Viewport viewport) {
        /* Does nothing */
    }
    
    /**
     * Does this action have a custom cursor?
     * @return whether this action has a custom cursor
     */
    public boolean hasCursor() {
        return false;
    }
}
