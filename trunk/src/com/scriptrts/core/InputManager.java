package com.scriptrts.core;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.event.MouseInputListener;

public class InputManager implements MouseInputListener, MouseWheelListener, KeyListener {
    
	/* Key listener states */
	ArrayList<Integer> registeredKeyCodes = new ArrayList<Integer>();
	HashMap<Integer, Boolean> keyCodeFlags = new HashMap<Integer, Boolean>();
	
	/* Singleton */
    private final static InputManager manager = new InputManager();

    /* Prevent more than one from being created */
    private InputManager(){
        super();
    }

    public static InputManager getInputManager(){
        return manager;
    }

    public void registerKeyCode(int code) {
    	registeredKeyCodes.add(code);
    	keyCodeFlags.put(code, false);
    }
    
    public boolean getKeyCodeFlag(int code) {
    	boolean flag = keyCodeFlags.get(code);
    	keyCodeFlags.put(code, false);
    	return flag;
    }

    /* Mouse methods */
    private boolean mouseMovement = false;
    private Point mouseLocation = new Point(0, 0);
    private boolean mousePressed = false;
    private boolean mouseClicked = false;
    private int mouseWheelScroll = 0;

    public boolean getMouseMoved(){
        boolean ret = mouseMovement;
        mouseMovement = false;
        return ret;
    }

    public Point getMouseLocation(){
        return mouseLocation;
    }

    public boolean getMouseClicked(){
        boolean ret = mouseClicked;
        mouseClicked = false;
        return ret;
    }
    
    public boolean getMouseDown(){
        return mousePressed;
    }

    public int getMouseScrollDistance(){
        int scroll = mouseWheelScroll;
        mouseWheelScroll = 0;
        return scroll;
    }

    public boolean getMouseScrolled(){
        return (mouseWheelScroll != 0);
    }

    /* Key listener */
    public void keyPressed(KeyEvent key){
    	keyCodeFlags.put(key.getKeyCode(), true);
    }
    public void keyReleased(KeyEvent key){}
    public void keyTyped(KeyEvent key){}

    /* Mouse listener */
    public void mouseEntered(MouseEvent mouse){
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }
    public void mouseExited(MouseEvent mouse){
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }

    public void mouseClicked(MouseEvent mouse){
        mouseClicked = true;
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    } 
    public void mousePressed(MouseEvent mouse){
        mousePressed = true;
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }
    public void mouseReleased(MouseEvent mouse){
        mousePressed = false;
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }
    public void mouseDragged(MouseEvent mouse){
        mouseMovement = true;
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }
    public void mouseMoved(MouseEvent mouse){
        mouseMovement = true;
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }

    /* Mouse wheel listener */
    public void mouseWheelMoved(MouseWheelEvent e){
        mouseWheelScroll += e.getWheelRotation();
    }



}
