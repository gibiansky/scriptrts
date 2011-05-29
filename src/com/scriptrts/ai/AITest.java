package com.scriptrts.ai;

import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class AITest extends JFrame {

	private Agent[] agents;
	private TestPanel panel;
	
	private static final int MAX_AGENTS = 30; 
	
	public AITest() {
		setSize(800, 600);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		panel = new TestPanel();
		add(panel);
		
		agents = new Agent[MAX_AGENTS];
		
	}
	
	private class TestPanel extends JPanel {
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.drawString("herro zahg", 50, 50);
		}
		
	}
	
	public static void main(String... args) {
		new AITest();
	}
	
}
