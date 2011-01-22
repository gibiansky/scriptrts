package com.scriptrts.core;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.event.MouseInputListener;

public class InputManager implements MouseInputListener, MouseWheelListener, KeyListener {
    /* Singleton */
    private final InputManager manager = new InputManager();

    /* Prevent more than one from being created */
    private InputManager(){
        super();
    }

    public InputManager getInputManager(){
        return manager;
    }

    /* Key listener */
    public void keyPressed(KeyEvent key){}
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
