package com.scriptrts.core.map;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.scriptrts.core.InputManager;

public class TestMap extends JFrame {

	int n = 1280, tilesize = 32, width=800, height=600;
	String[][] terrain;
	BufferedImage dirt, grass;
	BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	Graphics2D ig = (Graphics2D) buf.getGraphics();
	InputManager manager = InputManager.getInputManager();

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

		boolean fullscreen = JOptionPane.showConfirmDialog(null, "Enable Full Screen display?", "Fullscreen?", JOptionPane.YES_NO_OPTION) == 0;
		fullscreen = false;
		if(fullscreen){
			/* Disable resizing and decorations */
			setUndecorated(true);
			setResizable(false);

			/* Switch to fullscreen and make window maximum size */
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Dimension scrnsize = toolkit.getScreenSize();
			width = scrnsize.width;
			height = scrnsize.height;
			setSize(width, height);

			GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			device.setFullScreenWindow(this);
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
		// set up key listeners
		addKeyListener(manager);
		manager.registerKeyCode(KeyEvent.VK_LEFT);
		manager.registerKeyCode(KeyEvent.VK_RIGHT);
		manager.registerKeyCode(KeyEvent.VK_UP);
		manager.registerKeyCode(KeyEvent.VK_DOWN);
		// initial map paint
		for(int i=0; i<n; i++) {
			for(int j=0; j<n; j++) {
				ig.drawImage(terrain[i][j]=="dirt" ? dirt : grass, i*tilesize, j*tilesize, null);
			}
		}
		while(true) {
			// do stuff here
			if(manager.getKeyCodeFlag(KeyEvent.VK_RIGHT))
				;
			repaint();
		}
	}

	private class MapPanel extends JPanel {

		@Override
		protected void paintComponent(Graphics g) {
			g.drawImage(buf, 0, 0, width, height, null);
		}

	}

}
