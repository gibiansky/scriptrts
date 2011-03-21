package com.scriptrts.core.ui;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.ArrayList;

import com.scriptrts.util.ResourceManager;

/**
 * A button that can be painted onto the screen.
 */
public class ImageButton {
    /**
     * Width of the button
     */
    private int width;

    /**
     * Height of the button
     */
    private int height;

    /**
     * Location of the button on the screen
     */
    private int x, y;

    /**
     * The parent component
     */
    private Component parent;

    /**
     * Input manager used to determine whether the button is being pressed
     */
    private InputManager manager = InputManager.getInputManager();

    /**
     * Image to display when the button is clicked on
     */
    private BufferedImage downStateImage;

    /**
     * Image to display when the button is moused over
     */
    private BufferedImage highlightedStateImage;

    /**
     * Default image to display
     */
    private BufferedImage defaultStateImage;

    /**
     * The list of listeners to notify on button press
     */
    private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

    /**
     * Create a new button
     * @param defImg image used to draw button in normal state
     * @param highImg image used to draw button when it's moused over
     * @param clickedImg image used to draw button when button is clicked on
     * @param width width of button
     * @param height height of button
     * @param x x position of the image
     * @param y y position of the image
     */
    public ImageButton(BufferedImage defImg, BufferedImage highImg, BufferedImage clickedImg, int width, int height, int x, int y){
        super();

        this.defaultStateImage = ResourceManager.scaleImage(defImg, width, height);
        this.downStateImage = ResourceManager.scaleImage(clickedImg, width, height);
        this.highlightedStateImage = ResourceManager.scaleImage(highImg, width, height);
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
    }

    /**
     * Create a new button
     * @param defImg image used to draw button in normal state
     * @param highImg image used to draw button when it's moused over
     * @param clickedImg image used to draw button when button is clicked on
     * @param scale scale factor to apply to image
     * @param x x position of the image
     * @param y y position of the image
     */
    public ImageButton(BufferedImage defImg, BufferedImage highImg, BufferedImage clickedImg, double scale, int x, int y){
        this(defImg, highImg, clickedImg, (int) (defImg.getWidth() * scale), (int) (defImg.getHeight() * scale), x, y);
    }

    /**
     * Get the width of the button.
     * @return button width
     */
    public int getWidth(){
        return width;
    }

    /**
     * Get the height of the button.
     * @return button height
     */
    public int getHeight(){
        return height;
    }
    
    /**
     * Paint the button on the screen
     * @param graphics Graphics object used to draw to the screen
     */
    public void paint(Graphics2D graphics){
        paint(graphics, null);
    }

    /**
     * Paint the button on the screen with a given operation performed first
     * @param graphics Graphics object used to draw to the screen
     * @param op BufferedImageOp to apply to the image before drawing
     */
    public void paint(Graphics2D graphics, BufferedImageOp op){
        /* Get the mouse location relative to the parent component */
        Point mouse = manager.getMouseLocation();
        if(parent != null){
            Point componentLocation = parent.getLocation();
            mouse.translate(-componentLocation.x, -componentLocation.y);
        }
        
        BufferedImage buttonState;

        /* Check if it's inside the button */
        if(mouse.x >= x && mouse.x <= x + width && mouse.y >= y && mouse.y <= y + height){
            buttonState = highlightedStateImage;

            /* Check if it's clicked */
            if(manager.getMouseDown())
                buttonState = downStateImage;

            /* Notify listener if button was clicked */
            if(manager.getMouseClicked() && listeners.size() != 0)
                for(ActionListener listener : listeners)
                    listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Click"));

        } else
            buttonState = defaultStateImage;

        if(op == null)
            graphics.drawImage(buttonState, x, y, null);
        else
            graphics.drawImage(buttonState, op, x, y);
    }

    /**
     * Tell the image button the its parent component
     * @param component the image buttons parent
     */
    public void setParentComponent(Component component){
        parent = component;
    }

    /**
     * Get whether the mouse is pressed
     * @return whether the mouse is pressed
     */
    public boolean isPressed(){
        Point mouse = manager.getMouseLocation();
        return(mouse.x >= x && mouse.x <= x + width && mouse.y >= y && mouse.y <= y + height);
    }

    /** 
     * Add an action listener
     * @param list the action listener to add
     */
    public void addActionListener(ActionListener list){
        if(!listeners.contains(list))
            listeners.add(list);
    }

    /**
     * Set the position
     * @param x x coordinate
     * @param y y coordinate
     */
    public void setLocation(int x, int y){
        this.x = x;
        this.y = y;
    }

}
