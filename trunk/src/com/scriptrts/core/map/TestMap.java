package com.scriptrts.core.map;

import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class TestMap extends JFrame {

	final int n = 1280;
	String[][] terrain;
	
	public TestMap() {
		terrain = new String[n][n];
		for(int i=0; i<n; i++) {
			for(int j=0; j<n; j++) {
				if(Math.random() < 0.5)
					terrain[i][j] = "dirt";
				else
					terrain[i][j] = "grass";
			}
		}
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
			
	}
	
	public static void main(String... args) {
		TestMap app = new TestMap();
	}
	
	private class MapPanel extends JPanel {
		
		@Override
		protected void paintComponent(Graphics g) {
			
		}
		
	}
	
}
