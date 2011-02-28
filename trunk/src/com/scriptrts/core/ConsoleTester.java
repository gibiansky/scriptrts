package com.scriptrts.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ConsoleTester extends JFrame implements KeyListener {

	private static final int WIDTH = 800, HEIGHT = 600;
	private MyPanel panel;
	private Timer timer;

	/* Whether or not the console is currently showing */
	private boolean isConsoleDown;
	/* The text currently entered */
	private String buffer;
	/* The text previously entered */
	private ArrayList<String> cmdHistory;
	/* The previous output */
	private ArrayList<String> outputHistory;


	public static void main(String... args) {
		ConsoleTester test = new ConsoleTester();
	}

	public ConsoleTester() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		pack();
		setVisible(true);
		panel = new MyPanel();
		add(panel);
		addKeyListener(this);
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				repaint();
			}
		}, 0, 75);	
	}

	private class MyPanel extends JPanel {
		public void paint(Graphics g) {
			g.setColor(Color.white);
			g.fillRect(0, 0, getWidth(), getHeight());
			if(isConsoleDown) {
				g.setColor(new Color(0, 0, 0, 220));
				g.fillRect(0, 0, getWidth(), getHeight() / 5);
			}
		}
	}

	public void keyPressed(KeyEvent evt) {
		if(evt.getKeyCode() == KeyEvent.VK_BACK_QUOTE)
			isConsoleDown = ! isConsoleDown;
        else {
            int asciiVal = (int) evt.getKeyChar();
            if(isConsoleDown && asciiVal <= 125 && asciiVal >= 32) {
                String charEntered = evt.getKeyChar() + "";
                System.out.println(charEntered);
            }
        }
	}

	public void keyReleased(KeyEvent evt) {

	}

	public void keyTyped(KeyEvent evt) {

	}
}
