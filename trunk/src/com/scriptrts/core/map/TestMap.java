package com.scriptrts.core.map;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TestMap extends JFrame {

	final int n = 1280, tilesize = 32, width=800, height=600;
	int x,y;
	String[][] terrain;
	BufferedImage dirt, grass;
	BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	Graphics2D ig = (Graphics2D) buf.getGraphics();
	
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
		setSize(width, height);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		add(new MapPanel());
		
		gameLoop();
	}
	
	public static void main(String... args) {
		TestMap app = new TestMap();
	}
	
	private void gameLoop() {
		// load textures
		dirt = grass = null;
		try {
			dirt = ImageIO.read(new File("resource/map/dirt32.png"));
			grass = ImageIO.read(new File("resource/map/grass32.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		x=y=0;
		// initial map paint
		for(int i=0; i<n; i++) {
			for(int j=0; j<n; j++) {
				ig.drawImage(terrain[i][j]=="dirt" ? dirt : grass, i*tilesize, j*tilesize, null);
			}
		}
		while(true) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			repaint();
		}
	}
	
	private class MapPanel extends JPanel {
		
		@Override
		protected void paintComponent(Graphics g) {
			g.drawImage(buf, 0, 0, null);
		}
		
	}
	
}
