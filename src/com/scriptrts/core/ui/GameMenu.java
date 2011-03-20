package com.scriptrts.core.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.JPanel;

import com.scriptrts.util.ResourceManager;

/**
 * Button panel which pops up in the center of the screen when the user clicks on a menu bar item
 */
public class GameMenu extends JPanel {
    /**
     * Buttons to draw on the menu
     */
    private ImageButton[] buttons;

    /**
     * Image to use as the game menu background
     */
    private BufferedImage background;

    /**
     * Opacity of this game menu
     */
    private float opacity = 1.0f;

    /**
     * Create a new game menu
     * @param buts list of buttons to position on the menu
     * @param size what size to scale the menu to (in pixels wide)
     */
	public GameMenu(ImageButton[] buts, int size) {
        super(true);

        buttons = buts;

        /* Set size */
		setSize(size, size);
		setPreferredSize(getSize());
		
        /* Use absolute layout */
		setLayout(null);

        /* Set button locations  */
        int defSize = 538;
        int defCenter = 263;
        int defTop = 95;
        int center = (int) ((double)(defCenter) / defSize * getPreferredSize().width);
        int top = (int) ((double)(defTop) / defSize * getPreferredSize().width);
        int verticalMargin = 20;

        for(int i = 0; i < buttons.length; i++){
            int y = top;
            int x = center - buttons[i].getWidth() / 2;
            buttons[i].setLocation(x, y);

            top += buttons[i].getHeight() + verticalMargin;
        }

        /* Load the background image */
        try {
            background = ResourceManager.loadImage("resource/GameMenuBackground.png", size, size);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Set the buttons parent to be this game menu */
        for(ImageButton button : buttons)
            button.setParentComponent(this);
	}

    /**
     * Get the horizontal offset to the center of the menu.
     * @return horizontal offset to center
     */
    public int getCenterHorizontalOffset(){
        int defSize = 538;
        int defCenter = 263;
        int defTop = 95;
        int center = (int) ((double)(defCenter) / defSize * getPreferredSize().width);
        return center;
    }

    /**
     * Set the opacity of this game window
     * @param opacity desired opacity, from 0 (transparent) to 1 (fully opaque)
     */
    public void setOpacity(double opacity){
        this.opacity = (float) opacity;
    }

    /**
     * Draw the game menu on the screen
     * @param g graphics handle to the screen
     */
    protected void paintComponent(Graphics g){
        Graphics2D graphics = (Graphics2D) g;

        /* Create a rescale filter op that makes the image partially transparent */
        float[] scales = { 1f, 1f, 1f, opacity };
        float[] offsets = new float[4];
        RescaleOp opacityOp = new RescaleOp(scales, offsets, null);

        graphics.drawImage(background, opacityOp, 0, 0);

        for(ImageButton button : buttons)
            button.paint(graphics, opacityOp);
    }
}
