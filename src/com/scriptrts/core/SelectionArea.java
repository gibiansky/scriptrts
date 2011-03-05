package com.scriptrts.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import java.util.ArrayList;

import com.scriptrts.game.SimpleUnit;
import com.scriptrts.control.Selection;

/**
 * Selection area panel which display all units which are currently selected
 */
public class SelectionArea extends JPanel {
    /**
     * Width of the area
     */
    private int width = 400;

    /**
     * Height of the area
     */
    private int height = 200;

    /**
     * Cached image
     */
    private BufferedImage image;

    /**
     * Whether the selection has changed
     */
    private static boolean changedSelection = true;

    /**
     * Create a new selection area
     */
    public SelectionArea(){
        super(true);

        addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent mouse){
                mouseClick(mouse.getX(), mouse.getY());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseMoved(MouseEvent mouse){
                mouseHover(mouse.getX(), mouse.getY());
            }
        });

    }

    /**
     * Tell the selection area classes that the selection has changed.
     */
    public static void selectionChanged(){
        changedSelection = true;
    }
     

    /**
     * Draw the selection area
     * @param g graphics handle for the screen
     */
    public void paintComponent(Graphics g){
        if(changedSelection)
            redrawSelection();

        Graphics2D graphics = (Graphics2D) g;
        graphics.drawImage(image, 0, 0, null);
    }

    /**
     * Calculate the selection area image
     */
    private void redrawSelection(){
        /* After this redraw, no more updated needed until next change */
        changedSelection = false;

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        
        
        ArrayList<SimpleUnit> units = Selection.current().getObjects();

        /* For single unit select, draw unit info screen */
        if(units.size() == 1){
            SimpleUnit unit = units.get(0);
            int size = 180;
            int x = 20, y = 20;
            graphics.drawImage(unit.getArt(), x, y, size, size, null);
        } 
        
        /* Draw all units for multiple unit select */
        else {
            /* Unit size */
            int size = 60;
            int verticalMargin = 5;
            int horizontalMargin = calculateMargin(units.size(), size);
            int rows = height / (size+verticalMargin);
            int unitsPerRow = width / (size+horizontalMargin);

            /* Draw each unit */
            for(int i = 0; i < units.size(); i++){
                int col = i % unitsPerRow;
                int x = col * size + (col) * horizontalMargin + 5;

                int row = i / unitsPerRow;
                int y = row * size + (row+1) * verticalMargin;

                graphics.drawImage(units.get(i).getArt(), x, y, size, size, null);
            }
        }
    }

    /**
     * Calculate how much space there should be between unit icons
     * @return pixels between units; can be negative to make them stack
     */
    private int calculateMargin(int units, int size){
        final int[] margins = {
            6, -10, -26, -42, -54, -58
        };
        for(int margin : margins){
            int unitsPerRow = width / (size+margin);
            int rowsFilled = units / unitsPerRow;

            /* If it doesn't fit, reject it */
            if(rowsFilled > 3 || (rowsFilled == 3 && units % unitsPerRow != 0)) 
                continue;

            /* If it works, use it */
            else
                return margin;
        }

        return -58;
    }

    /**
     * Resize based on window
     */
    public void setWidth(int w){
        this.width = w - 5;
    }

    /**
     * Select a unit that was clicked on
     * @param x x coordinate of click
     * @param y y coordinate of click
     */
    public void mouseClick(int x, int y){
    }

    /**
     * Display info about a unit that was hovered over
     * @param x x coordinate of click
     * @param y y coordinate of click
     */
    public void mouseHover(int x, int y){

    }

    /**
     * Get the desired size of the minimap
     */
    public Dimension getPreferredSize(){
        return new Dimension(width, height);
    }
}
