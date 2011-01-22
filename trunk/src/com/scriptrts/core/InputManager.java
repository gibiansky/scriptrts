package com.scriptrts.core;

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
    
    /* Key listener */
    public void keyPressed(KeyEvent key){
    	keyCodeFlags.put(key.getKeyCode(), true);
    }
    public void keyReleased(KeyEvent key){}
    public void keyTyped(KeyEvent key){}

    /* Mouse listener */
    public void mouseClicked(MouseEvent mouse){} 
    public void mouseEntered(MouseEvent mouse){}
    public void mouseExited(MouseEvent mouse){}
    public void mousePressed(MouseEvent mouse){}
    public void mouseReleased(MouseEvent mouse){}
    public void mouseDragged(MouseEvent mouse){}
    public void mouseMoved(MouseEvent mouse){}

    /* Mouse wheel listener */
    public void mouseWheelMoved(MouseWheelEvent e){

    }



}
