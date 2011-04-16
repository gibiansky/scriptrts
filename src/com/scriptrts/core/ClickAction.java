package com.scriptrts.core;

import java.awt.Graphics;

import com.scriptrts.core.ui.Viewport;
import com.scriptrts.game.GameObject;

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
     * @param viewport viewport used to display map
     * @param x x coordinate of mouse
     * @param y y coordinate of mouse
     */
    public void paintCursor(Graphics graphics, Viewport viewport, int x, int y) {
        /* Does nothing */
    }
    
    /**
     * Does this action have a custom cursor?
     * @return whether this action has a custom cursor
     */
    public boolean hasCursor() {
        return false;
    }
    
    public GameObject getUnit(){
		return null;
    }
}
