package com.scriptrts.core;

import java.awt.Point;
import java.awt.event.KeyEvent;
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
    	return flag;
    }

    public void clearKeyCodeFlag(int code) {
    	keyCodeFlags.put(code, false);
    }

    /* Mouse methods */
    private boolean mouseMovement = false;
    private Point mouseLocation = new Point(0, 0);

    private boolean mousePressedRight = false;
    private boolean mouseClickedRight = false;
    private boolean mousePressedLeft = false;
    private boolean mouseClickedLeft = false;

    private boolean mouseBeingDragged = false;
    private int mouseWheelScroll = 0;

    public boolean getMouseMoved(){
        boolean ret = mouseMovement;
        mouseMovement = false;
        return ret;
    }

    public boolean getMouseDragged(){
        return mouseBeingDragged;
    }

    public Point getMouseLocation(){
        return new Point(mouseLocation);
    }

    public boolean getMouseClicked(){
        boolean ret = mouseClickedRight || mouseClickedLeft;
        mouseClickedRight = mouseClickedLeft = false;
        return ret;
    }
    
    public boolean getRightMouseClicked(){
        boolean ret = mouseClickedRight;
        mouseClickedRight = false;
        return ret;
    }

    public boolean getLeftMouseClicked(){
        boolean ret = mouseClickedLeft;
        mouseClickedLeft = false;
        return ret;
    }

    public boolean getMouseDown(){
        return mousePressedRight || mousePressedLeft;
    }

    public boolean getLeftMouseDown(){
        return mousePressedLeft;
    }

    public boolean getRightMouseDown(){
        return mousePressedRight;
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
    public void keyReleased(KeyEvent key){
    	keyCodeFlags.put(key.getKeyCode(), false);
    }
    public void keyTyped(KeyEvent key){
    }

    /* Mouse listener */
    public void mouseEntered(MouseEvent mouse){
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }
    public void mouseExited(MouseEvent mouse){
        mouseBeingDragged = false;
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }

    public void mouseClicked(MouseEvent mouse){
        if(mouse.getButton() == MouseEvent.BUTTON1)
            mouseClickedLeft = true;
        else if(mouse.getButton() == MouseEvent.BUTTON3)
            mouseClickedRight = true;
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    } 
    public void mousePressed(MouseEvent mouse){
        if(mouse.getButton() == MouseEvent.BUTTON1)
            mousePressedLeft = true;
        else if(mouse.getButton() == MouseEvent.BUTTON3)
            mousePressedRight = true;
        System.out.println(mousePressedLeft);
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }
    public void mouseReleased(MouseEvent mouse){
        if(mouse.getButton() == MouseEvent.BUTTON1)
            mousePressedLeft = false;
        else if(mouse.getButton() == MouseEvent.BUTTON3)
            mousePressedRight = false;
        mouseBeingDragged = false;
        mouseLocation.x = mouse.getX();
        mouseLocation.y = mouse.getY();
    }
    public void mouseDragged(MouseEvent mouse){
        mouseMovement = true;
        mouseBeingDragged = true;
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
