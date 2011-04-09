package com.scriptrts.core.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
     * The selection area shown when multiple units are selected
     */
    private SelectionArea selectionArea;

    /**
     * The button area used to show buttons
     */
    private ButtonArea buttonArea;
    

    /**
     * Create a new top bar
     * @param viewport viewport used to display the screen
     */
    public OverlayPane(Viewport viewport) {
        super(true);
        setLayout(null);

        this.width = viewport.getWidth();

        /* Load images */
        try {
            barBackground = ResourceManager.loadImage("resource/OverlayBackgroundCenter.png");
            barBackgroundLeft = ResourceManager.loadImage("resource/OverlayBackgroundLeft.png");
            barBackgroundRight = ResourceManager.loadImage("resource/OverlayBackgroundRight.png");
        } catch (Exception e) { e.printStackTrace(); }

        minimap = new Minimap(viewport);
        Dimension size = minimap.getPreferredSize();
        minimap.setBounds(width - size.width, 5, size.width, size.height);
        int minimapX = width - size.width;
        add(minimap);

        buttonArea = new ButtonArea(this);
        size = buttonArea.getPreferredSize();
        buttonArea.setBounds(0, 0, size.width, size.height);
        add(buttonArea);

        selectionArea = new SelectionArea();
        selectionArea.setWidth(minimapX - size.width);
        size = selectionArea.getPreferredSize();
        selectionArea.setBounds(minimapX - size.width, 0, size.width, size.height);
        add(selectionArea);
    }

    /**
     * Resize the width of the bar
     * @param width new width of the bar
     */
    public void setWidth(int width){
        this.width = width;

        Dimension size = minimap.getPreferredSize();
        minimap.setBounds(width - size.width, 5, size.width, size.height);
        int minimapX = width - size.width;

        size = buttonArea.getPreferredSize();
        buttonArea.setBounds(0, 0, size.width, size.height);

        selectionArea.setWidth(minimapX - size.width);
        size = selectionArea.getPreferredSize();
        selectionArea.setBounds(minimapX - size.width, 0, size.width, size.height);
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

        int imgWidth = barBackgroundLeft.getWidth() - 3;
        graphics.drawImage(barBackgroundLeft, 0, 0, null);

        for(int i = imgWidth; i <= width; i += imgWidth){
            graphics.drawImage(barBackground, i, 0, null);
        }

        graphics.drawImage(barBackgroundRight, width - imgWidth, 0, null);
    }

    /**
     * Get the size of the component
     * @return size of the top bar
     */
    public Dimension getPreferredSize(){
        return new Dimension(width, height + 10);
    }
}
