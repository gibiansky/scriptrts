package com.scriptrts.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.scriptrts.util.ResourceManager;

/** 
 * Main bottom overlay
 */
public class OverlayPane extends JPanel {
    /**
     * Width of the overlay (equal to width of the screen)
     */
    private int width;

    /**
     * Height of the overlay
     */
    private int height = 200;

    /**
     * Viewport used to display the game
     */
    private Viewport viewport;

    /**
     * The generic background.
     */
    private BufferedImage barBackground;

    /**
     * The generic left corner
     */
    private BufferedImage barBackgroundLeft;
    
    /**
     * The generic right corner
     */
    private BufferedImage barBackgroundRight;

    /**
     * The minimap to display on the right
     */
    private Minimap minimap;

    /**
     * Create a new top bar
     * @param viewport viewport used to display the screen
     */
    public OverlayPane(Viewport viewport) {
        super(true);
        setLayout(null);

        this.viewport = viewport;
        this.width = viewport.getWidth();

        /* Load images */
        try {
            barBackground = ResourceManager.loadImage("resource/MenuBackgroundCenter.png");
            barBackgroundLeft = ResourceManager.loadImage("resource/MenuBackgroundLeft.png");
            barBackgroundRight = ResourceManager.loadImage("resource/MenuBackgroundRight.png");
        } catch (Exception e) { e.printStackTrace(); }

        minimap = new Minimap(viewport);
        Dimension size = minimap.getPreferredSize();
        minimap.setBounds(width - size.width, 0, size.width, size.height);
        add(minimap);
    }

    /**
     * Resize the width of the bar
     * @param width new width of the bar
     */
    public void setWidth(int width){
        this.width = width;

        Dimension size = minimap.getPreferredSize();
        minimap.setBounds(width - size.width, 0, size.width, size.height);
    }

    /**
     * Add a key listener to all parts of this panel
     * @param listener key listenter to notify on events
     */
    public void addKeyListener(KeyListener listener){
        addKeyListener(listener);
    }

    /**
     * Draw the component on the screen.
     * @param g graphics handle used to draw on the screen
     */
    public void paintComponent(Graphics g){
        Graphics2D graphics = (Graphics2D) g;

        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, width, height);

        int vertical = viewport.getHeight() - height;
        int scale = barBackgroundLeft.getHeight() / 30;
        int imgWidth = barBackgroundLeft.getWidth() / scale;
        int imgHeight = barBackgroundLeft.getHeight() / scale;
        graphics.drawImage(barBackgroundLeft, 0, 0, imgWidth, imgHeight, null);

        int i = imgWidth;
        imgWidth = barBackground.getWidth() / scale;
        imgHeight = barBackground.getHeight() / scale;
        for(; i <= width - imgWidth; i += imgWidth){
            graphics.drawImage(barBackground, i, 0, imgWidth, imgHeight, null);
        }

        imgWidth = barBackgroundRight.getWidth() / scale;
        imgHeight = barBackgroundRight.getHeight() / scale;
        graphics.drawImage(barBackgroundRight, width - imgWidth, 0, imgWidth, imgHeight, null);
    }

    /**
     * Get the size of the component
     * @return size of the top bar
     */
    public Dimension getPreferredSize(){
        return new Dimension(width, height);
    }

    // FIXME remove me i'm just here for lazy debugs
    public Dimension minimapSize() {
    	return minimap.getSize();
    }
    public Rectangle minimapBounds() { return minimap.getBounds(); }
}
