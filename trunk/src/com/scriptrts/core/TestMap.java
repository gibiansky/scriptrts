package com.scriptrts.core;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

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

        boolean fullscreen = false;
		//boolean fullscreen = JOptionPane.showConfirmDialog(null, "Enable Full Screen display?", "Fullscreen?", JOptionPane.YES_NO_OPTION) == 0;
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
        addMouseMotionListener(manager);
        addMouseListener(manager);
		manager.registerKeyCode(KeyEvent.VK_LEFT);
		manager.registerKeyCode(KeyEvent.VK_RIGHT);
		manager.registerKeyCode(KeyEvent.VK_UP);
		manager.registerKeyCode(KeyEvent.VK_DOWN);

        // Affine transform
        double a = 1/Math.sqrt(6);
        double[] matrx = {Math.sqrt(3) * a, 0, -a, 2*a, 250, 80};
        AffineTransform projection = new AffineTransform(matrx);
        ig.setTransform(projection);

		
        int leftBoundary = 0, topBoundary = 0;
		while(true) {
			// do stuff here
			if(manager.getKeyCodeFlag(KeyEvent.VK_RIGHT)) {
                leftBoundary++;
                if(leftBoundary >= n) leftBoundary = n;
            }
			if(manager.getKeyCodeFlag(KeyEvent.VK_LEFT)) {
                leftBoundary--;
                if(leftBoundary <= 0) leftBoundary = 0;
            }
			if(manager.getKeyCodeFlag(KeyEvent.VK_UP)) {
                topBoundary--;
                if(topBoundary <= 0) topBoundary = 0;
            }
			if(manager.getKeyCodeFlag(KeyEvent.VK_DOWN)) {
                topBoundary++;
                if(topBoundary >= n) topBoundary = n;
            }

            for(int i = leftBoundary; i < leftBoundary + width/tilesize + 1; i++) {
                for(int j = topBoundary; j < topBoundary + height/tilesize + 1; j++) {
                    ig.drawImage(terrain[i][j] == "dirt" ? dirt : grass, (i - leftBoundary)*tilesize, (j - topBoundary)*tilesize, null);
                }
            }
        
            repaint();

            try {
            Thread.sleep(30);
            } catch(Exception e) {e.printStackTrace();}
		}
	}

	private class MapPanel extends JPanel {

		@Override
		protected void paintComponent(Graphics g) {
			g.drawImage(buf, 0, 0, width, height, null);
		}

	}

}
