package com.scriptrts.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.KeyListener;
import javax.swing.JPanel;

import com.scriptrts.util.ResourceManager;

/** 
 * Top bar interface element
 */
public class TopBar extends JPanel {
    /**
     * Width of the top bar (equal to width of the screen)
     */
    private int width;

    /**
     * Height of the top bar
     */
    private int height = 30;

    /**
     * The generic menu bar background.
     */
    private BufferedImage barBackground;

    /**
     * The generic menu bar left corner
     */
    private BufferedImage barBackgroundLeft;
    
    /**
     * The generic menu bar right corner
     */
    private BufferedImage barBackgroundRight;

    /**
     * The individual button images (overlayed onto the generic background)
     */
    private ImageButton[] buttons;

    /**
     * Create a new top bar
     * @param width width of the screen
     */
    public TopBar(int width) {
        super(true);

        this.width = width;

        /* Load images */
        try {
            barBackground = ResourceManager.loadImage("resource/MenuBackgroundCenter.png");
            barBackgroundLeft = ResourceManager.loadImage("resource/MenuBackgroundLeft.png");
            barBackgroundRight = ResourceManager.loadImage("resource/MenuBackgroundRight.png");

            BufferedImage img = ResourceManager.loadImage("resource/TotalButton.png");
            BufferedImage img2 = ResourceManager.loadImage("resource/TotalButton2.png");
            BufferedImage img3 = ResourceManager.loadImage("resource/TotalButton3.png");
            buttons = new ImageButton[3];
            for(int i = 0; i < buttons.length; i++){
                int scale = 3;
                int horizontal = width - (i+1) * img.getWidth() / scale;
                ImageButton button = new ImageButton(img, img2, img3, 1/3.0, horizontal, 0);
                buttons[i] = button;

                final int j = i;
                buttons[i].addActionListener(new java.awt.event.ActionListener(){
                    public void actionPerformed(java.awt.event.ActionEvent e){
                        System.out.println("Menu button pressed!");
                    }
                });
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Resize the width of the bar
     * @param width new width of the bar
     */
    public void setWidth(int width){
        this.width = width;


        try {
            BufferedImage img = ResourceManager.loadImage("resource/TotalButton.png");
            for(int i = 0; i < buttons.length; i++){
                int scale = 3;
                int horizontal = width - (i+1) * img.getWidth() / scale;
                buttons[i].setLocation(horizontal, 0);
            }
        } catch (Exception e) { }
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

        for(int j = 0; j < buttons.length; j++){
            buttons[j].paint(graphics);
        }
    }

    /**
     * Get the size of the component
     * @return size of the top bar
     */
    public Dimension getPreferredSize(){
        return new Dimension(width, height);
    }

}