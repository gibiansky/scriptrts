package com.scriptrts.core.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import com.scriptrts.core.Main;
import com.scriptrts.core.MoveAction;
import com.scriptrts.core.StopAction;
import com.scriptrts.util.ResourceManager;

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
     * Input manager used to determine whether the button is being pressed
     */
    private InputManager manager = InputManager.getInputManager();


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
     * All the possible buttons in the game.
     */
    private static final Map<String, ImageButton> ALL_BUTTONS = loadButtons();

    /**
     * What this button panel is being drawn on
     */
    private OverlayPane overlay;

    /**
     * Previous point
     */
    private Point previous;

    /**
     * Create a new button area
     */
    public ButtonArea(OverlayPane overlay){
        super(true);
        this.overlay = overlay;

        previous = manager.getMouseLocation();
        buttons = new ArrayList<ImageButton>(ALL_BUTTONS.values()).toArray(buttons);
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
        if(!previous.equals(manager.getMouseLocation())){
            Point mouse = manager.getMouseLocation();
            mouseHover(mouse.x, mouse.y);
        }

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
        int buttonsPerRow = 4;

        /* Draw each button */
        for(int i = 0; i < 16; i++){
            int col = i % buttonsPerRow;
            int x = col * size + (col+1) * horizontalMargin;

            int row = i / buttonsPerRow;
            int y = row * size + (row+1) * verticalMargin - 2;

            if(i < buttons.length && buttons[i] != null){
                buttons[i].setLocation(x, y);
                buttons[i].setParentComponent(overlay);
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
        changedButtons = true;
    }

    /**
     * Get the desired size of the minimap
     */
    public Dimension getPreferredSize(){
        return new Dimension(width, height);
    }
    
    /**
     * Load all the button images.
     */
    private static HashMap<String, ImageButton> loadButtons(){
    	try{
    		int x = 0;
    		int y = 0;
    		
    		// Move button
    		BufferedImage moveImg =  ResourceManager.loadImage("resource/button/move.jpeg");
    		BufferedImage moveHoverImg = ResourceManager.loadImage("resource/button/move_hover.jpeg");
    		BufferedImage movePressImg = ResourceManager.loadImage("resource/button/move_press.jpeg");

    		double moveScale = 40.0/234;
    		
    		ImageButton moveButton = new ImageButton(moveImg, moveHoverImg, movePressImg, moveScale, x, y);
    		moveButton.addActionListener(new ActionListener(){
    			@Override
    			public void actionPerformed(ActionEvent a) {
    				Main.getGame().setClickAction(new MoveAction());
    			}
    		});
    		
    		// Stop button
    		BufferedImage stopImg =  ResourceManager.loadImage("resource/button/stop.png");
    		BufferedImage stopHoverImg = ResourceManager.loadImage("resource/button/stop_hover.png");
    		BufferedImage stopPressImg = ResourceManager.loadImage("resource/button/stop_press.png");

    		double stopScale = 40.0/300;
    		    		
    		ImageButton stopButton = new ImageButton(stopImg, stopHoverImg, stopPressImg, stopScale, x, y);
    		stopButton.addActionListener(new ActionListener(){
    			@Override
    			public void actionPerformed(ActionEvent a) {
    				Main.getGame().setClickAction(new StopAction());
    			}
    		});
    		

    		HashMap<String, ImageButton> m = new HashMap<String, ImageButton>();
    		m.put("move", moveButton);
    		m.put("stop", stopButton);
    		
    		return m;
    	}	catch(IOException e){
    		System.err.println("Can't load button image!");
    	}
    	return null;
    }
}

