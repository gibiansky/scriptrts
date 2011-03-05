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
 * Button area to display command buttons
 */
public class ButtonArea extends JPanel {
    /**
     * Width of the area
     */
    private int width = 200;

    /**
     * Height of the area
     */
    private int height = 200;

    /**
     * List of buttons to draw
     */
    private ImageButton[] buttons = new ImageButton[16];

    /**
     * Cached image
     */
    private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    /**
     * Whether the buttons need repainting
     */
    private static boolean changedButtons = true;

    /**
     * Create a new button area
     */
    public ButtonArea(){
        super(true);
        setBackground(Color.yellow);

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
     * Change the buttons on the button area
     */
    public void setButtons(ImageButton[] buttons){
        this.buttons = buttons;
        changedButtons = true;
    }
     

    /**
     * Draw the buttons on the screen
     * @param g graphics handle for the screen
     */
    public void paintComponent(Graphics g){
        if(changedButtons)
            redrawButtons();

        Graphics2D graphics = (Graphics2D) g;
        graphics.drawImage(image, 0, 0, null);
    }

    /**
     * Calculate the button image
     */
    private void redrawButtons(){
        /* After this redraw, no more updated needed until next change */
        changedButtons = false;

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        
        /* Button size */
        int size = 40;
        int verticalMargin = 10;
        int horizontalMargin = 10;
        int rows = 4;
        int buttonsPerRow = 4;

        /* Draw each unit */
        for(int i = 0; i < buttons.length; i++){
            int col = i % buttonsPerRow;
            int x = col * size + (col+1) * horizontalMargin;

            int row = i / buttonsPerRow;
            int y = row * size + (row+1) * verticalMargin - 2;

            if(buttons[i] != null){
                buttons[i].setLocation(x, y);
                buttons[i].paint(graphics);
            } else {
                graphics.setColor(Color.red);
                graphics.fillRect(x, y, size, size);
                graphics.setColor(Color.black);
            }
        }
    }

    /**
     * Resize based on window
     */
    public void setWidth(int w){
        width = w;
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

