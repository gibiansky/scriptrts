package com.scriptrts.core;

import java.awt.Graphics;

/**
 * Action that should be taken when the map is next clicked
 */
public interface ClickAction {
    /**
     * Method called when map is clicked
     * @param x x coordinate on map
     * @param y y coordinate on map
     */
    public void click(int x, int y);

    /**
     * Draws cursor for the action
     * @param graphics graphics handle on the screen
     */
    public void paintCursor(Graphics graphics);
}
