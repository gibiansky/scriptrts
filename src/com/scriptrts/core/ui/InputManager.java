package com.scriptrts.core.ui;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.event.MouseInputListener;

/**
 * Universal input manager for the entire user interface of the game.
 */
public class InputManager implements MouseInputListener, MouseWheelListener, KeyListener {
	/**
     * Singleton instance of the input manager.
     */
    private final static InputManager manager = new InputManager();

    /**
     * A list of key codes which this input manager should respond to and remember.
     */
	ArrayList<Integer> registeredKeyCodes = new ArrayList<Integer>();

    /**
     * A map that remembers which keys are currently pressed.
     */
	HashMap<Integer, Boolean> keyCodeFlags = new HashMap<Integer, Boolean>();
	
    /** 
     * Has the mouse been moved?
     */
    private boolean mouseMovement = false;

    /**
     * Time when mouse left button was last clicked
     */
    private long mouseLeftClickedTime = System.currentTimeMillis();
    
    /**
     * Time when mouse right button was last clicked
     */
    private long mouseRightClickedTime = System.currentTimeMillis();

    /**
     * The point where the mouse has been last seen.
     */
    private Point mouseLocation = new Point(0, 0);

    /**
     * Whether the right mouse is pressed.
     */
    private boolean mousePressedRight = false;

    /**
     * Whether the right mouse has been clicked.
     */
    private boolean mouseClickedRight = false;

    /**
     * Whether the left mouse is pressed.
     */
    private boolean mousePressedLeft = false;

    /**
     * Whether the left mouse has been clicked.
     */
    private boolean mouseClickedLeft = false;

    /**
     * Whether the mouse is currently being dragged.
     */
    private boolean mouseBeingDragged = false;

    /**
     * How many levels the mouse has been scrolled.
     */
    private int mouseWheelScroll = 0;

    /**
     * Create the singleton input manager.
     */
    private InputManager(){
        super();
    }

    /**
     * Get the input manager singleton.
     * @return input manager singleton
     */
    public static InputManager getInputManager(){
        return manager;
    }

    /**
     * Notify the input manager that this keycode should be kept track of.
     * @param code keycode to track.
     */
    public void registerKeyCode(int code) {
        if(!registeredKeyCodes.contains(code)){
            registeredKeyCodes.add(code);
            keyCodeFlags.put(code, false);
        }
    }
    
    /**
     * Get whether or not this key is pressed.
     * @return true if the key is pressed, false otherwise.
     */
    public boolean getKeyCodeFlag(int code) {
    	boolean flag = keyCodeFlags.get(code);
    	return flag;
    }

    /**
     * Clear the status of the key (as if it were unpressed).
     * @param code keycode to clear
     */
    public void clearKeyCodeFlag(int code) {
    	keyCodeFlags.put(code, false);
    }


    /**
     * Return whether the mouse has been moved.
     * @return true if mouse has been moved.
     */
    public boolean getMouseMoved(){
        boolean ret = mouseMovement;
        mouseMovement = false;
        return ret;
    }

    /**
     * Return whether the mouse is being dragged.
     * @return true if mouse is being dragged.
     */
    public boolean getMouseDragged(){
        return mouseBeingDragged;
    }

    /**
     * Return last seen mouse location.
     * @return Point where mouse was last
     */
    public Point getMouseLocation(){
        return new Point(mouseLocation);
    }

    /**
     * Return whether either mouse has been clicked.
     * @return true if left or right was clicked.
     */
    public boolean getMouseClicked(){
        boolean ret = mouseClickedRight || mouseClickedLeft;
        mouseClickedRight = mouseClickedLeft = false;

        if(System.currentTimeMillis() - mouseRightClickedTime > 200 && System.currentTimeMillis() - mouseLeftClickedTime > 200)
                return false;

        return ret;
    }
    
    /**
     * Return whether the right mouse was clicked.
     * @return true if right mouse was clicked.
     */
    public boolean getRightMouseClicked(){
        boolean ret = mouseClickedRight;
        mouseClickedRight = false;

        if(System.currentTimeMillis() - mouseRightClickedTime > 200)
            return false;

        return ret;
    }

    /**
     * Return whether the left mouse was clicked.
     * @return true if the left mouse was clicked.
     */
    public boolean getLeftMouseClicked(){
        boolean ret = mouseClickedLeft;
        mouseClickedLeft = false;

        if(System.currentTimeMillis() - mouseLeftClickedTime > 200)
            return false;

        return ret;
    }

    /**
     * Return whether any mouse key is down.
     * @return true if left or right mouse buttons are down.
     */
    public boolean getMouseDown(){
        return mousePressedRight || mousePressedLeft;
    }

    /**
     * Return whether the left mouse button was down.
     * @return true if left mouse button is pressed.
     */
    public boolean getLeftMouseDown(){
        return mousePressedLeft;
    }

    /**
     * Return whether the right mouse button is down.
     * @return true if mouse right button is down.
     */
    public boolean getRightMouseDown(){
        return mousePressedRight;
    }

    /**
     * Return how far the mouse has scrolled since last checked.
     * @return how far mouse has scrolled
     */
    public int getMouseScrollDistance(){
        int scroll = mouseWheelScroll;
        mouseWheelScroll = 0;
        return scroll;
    }

    /**
     * Return whether the mouse has scrolled.
     * @return true if the mouse has been scrolled.
     */
    public boolean getMouseScrolled(){
        return (mouseWheelScroll != 0);
    }

    /**
     * Method inhereted from the key listener 
     * @param key Key event from the Swing component
     */
    public void keyPressed(KeyEvent key){
    	keyCodeFlags.put(key.getKeyCode(), true);
    }

    /**
     * Method inhereted from the key listener 
     * @param key Key event from the Swing component
     */
    public void keyReleased(KeyEvent key){
    	keyCodeFlags.put(key.getKeyCode(), false);
    }

    /**
     * Method inhereted from the key listener 
     * @param key Key event from the Swing component
     */
    public void keyTyped(KeyEvent key){
    }

    /**
     * Method inherited from the mouse listener
     */
    public void mouseEntered(MouseEvent mouse){
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }

    /**
     * Method inherited from the mouse listener
     */
    public void mouseExited(MouseEvent mouse){
        mouseBeingDragged = false;
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }

    /**
     * Method inherited from the mouse listener
     * @param mouse MouseEvent passed from Swing component
     */
    public void mouseClicked(MouseEvent mouse){
        if(mouse.getButton() == MouseEvent.BUTTON1){
            mouseClickedLeft = true;
            mouseLeftClickedTime = System.currentTimeMillis();
        }
        else if(mouse.getButton() == MouseEvent.BUTTON3){
            mouseClickedRight = true;
            mouseRightClickedTime = System.currentTimeMillis();
        }
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    } 

    /**
     * Method inherited from the mouse listener
     * @param mouse MouseEvent passed from Swing component
     */
    public void mousePressed(MouseEvent mouse){
        if(mouse.getButton() == MouseEvent.BUTTON1)
            mousePressedLeft = true;
        else if(mouse.getButton() == MouseEvent.BUTTON3)
            mousePressedRight = true;
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }
    
    /**
     * Method inherited from the mouse listener
     * @param mouse MouseEvent passed from Swing component
     */
    public void mouseReleased(MouseEvent mouse){
        if(mouse.getButton() == MouseEvent.BUTTON1)
            mousePressedLeft = false;
        else if(mouse.getButton() == MouseEvent.BUTTON3)
            mousePressedRight = false;
        mouseBeingDragged = false;
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }

    /**
     * Method inherited from the mouse motion listener
     * @param mouse MouseEvent passed from Swing component
     */
    public void mouseDragged(MouseEvent mouse){
        mouseMovement = true;
        mouseBeingDragged = true;
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }

    /**
     * Method inherited from the mouse motion listener
     * @param mouse MouseEvent passed from Swing component
     */
    public void mouseMoved(MouseEvent mouse){
        mouseMovement = true;
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }

    /**
     * Method inherited from the mouse wheel listener
     * @param mouse MouseEvent passed from Swing component
     */
    public void mouseWheelMoved(MouseWheelEvent mouse){
        mouseWheelScroll += mouse.getWheelRotation();
    }

    public ArrayList<Integer> getRegisteredKeyCodes(){
    	return registeredKeyCodes;
    }

}
