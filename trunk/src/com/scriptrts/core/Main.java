package com.scriptrts.core;

import java.awt.geom.*;
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
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Main extends JPanel {
    /* Game properties */
    final static int n = 1280, tilesize = 32;
    final static JFrame window = new JFrame("ScriptRTS");

    /* Viewport properties */
    static int width=800, height=600;
    int viewportX = 0, viewportY = 0;

    /* Game data */
    String[][] terrain;
    BufferedImage dirt, grass;
    boolean initialized = false;

    /* Input manager */
    InputManager manager = InputManager.getInputManager();

    /* Perspective affine transform */
    AffineTransform projectionTransform = new AffineTransform(Math.sqrt(1/2.0), 0, -Math.sqrt(1/6.0), 2* Math.sqrt(1/6.0), 250, 80);
    AffineTransform inverseTransform;

    /* Create a new JPanel Main object with double buffering enabled */
    public Main() {
        super(true);
    }

    public static void main(String... args) {
        /* Create window of default size */
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(width, height);

        /* Check for fullscreen */
        boolean fullscreen = JOptionPane.showConfirmDialog(null, "Enable Full Screen display?", "Fullscreen?", JOptionPane.YES_NO_OPTION) == 0;

        if(fullscreen){
            /* Disable resizing and decorations */
            window.setUndecorated(true);
            window.setResizable(false);

            /* Switch to fullscreen and make window maximum size */
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension scrnsize = toolkit.getScreenSize();
            width = scrnsize.width;
            height = scrnsize.height;
            window.setSize(width, height);

            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            device.setFullScreenWindow(window);
        }

        /* Add graphics location */
        final Main panel = new Main();
        window.getContentPane().add(panel);

        /* Set window to be visible */
        window.setVisible(true);

        /* Initialize the game */
        panel.initializeGame();

        /* Start game loop */
        float fps = 30;
        TimerTask updateTask = new TimerTask(){
            public void run(){
                panel.updateGame();
                window.repaint();
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(updateTask, 0, (long) (1000 / fps));
    }

    /* Initialize game resources */
    public void initializeGame(){
        /* Create terrain */
        terrain = new String[n][n];
        for(int i=0; i<n; i++) {
            for(int j=0; j<n; j++) {
                if(Math.random() < 0.5)
                    terrain[i][j] = "dirt";
                else
                    terrain[i][j] = "grass";
            }
        }

        /* Initialize transforms */
        try {
            inverseTransform = projectionTransform.createInverse();
        } catch (Exception e) { e.printStackTrace(); System.exit(1); }

        // load textures
        dirt = grass = null;
        try {
            dirt = ImageIO.read(new File("resource/map/dirt32.png"));
            grass = ImageIO.read(new File("resource/map/grass32.png"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // set up key listeners
        window.addKeyListener(manager);
        window.addMouseMotionListener(manager);
        window.addMouseListener(manager);
        manager.registerKeyCode(KeyEvent.VK_LEFT);
        manager.registerKeyCode(KeyEvent.VK_RIGHT);
        manager.registerKeyCode(KeyEvent.VK_UP);
        manager.registerKeyCode(KeyEvent.VK_DOWN);

        /* Done with initialization */
        initialized = true;
    }

    /* Update game state */
    public void updateGame(){
        int increment = 15;
        if(manager.getKeyCodeFlag(KeyEvent.VK_RIGHT)) {
            viewportX += increment;
            if(viewportX >= n * tilesize - width) viewportX = n * tilesize - width;
        }
        if(manager.getKeyCodeFlag(KeyEvent.VK_LEFT)) {
            viewportX -= increment;
            if(viewportX <= 0) viewportX = 0;
        }
        if(manager.getKeyCodeFlag(KeyEvent.VK_UP)) {
            viewportY -= increment;
            if(viewportY <= 0) viewportY = 0;
        }
        if(manager.getKeyCodeFlag(KeyEvent.VK_DOWN)) {
            viewportY += increment;
            if(viewportY >= n * tilesize - height) viewportY = n * tilesize - height;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if(!initialized) return;

        /* Create a transformed graphics object to draw in perspective */
        Graphics2D graphics = (Graphics2D) g;
        AffineTransform flatTransform = graphics.getTransform();
        graphics.setTransform(projectionTransform);
 
        /* Find where to cull the edges of the map */
        Point2D topLeft = new Point2D.Double(viewportX, viewportY);
        Point2D bottomRight = new Point2D.Double(topLeft.getX() + width, topLeft.getY() + height);
        inverseTransform.transform(topLeft, topLeft);
        inverseTransform.transform(bottomRight, bottomRight);

        /* Find boundaries of map */
        int leftBoundary = (int) (topLeft.getX() / tilesize) - 1, topBoundary = (int) (topLeft.getY() / tilesize) - 1;
        if(leftBoundary < 0) leftBoundary = 0;
        if(topBoundary < 0) topBoundary = 0;

        int rightBoundary = (int) (bottomRight.getX() / tilesize) + 1, bottomBoundary = (int) (bottomRight.getY() / tilesize) + 1;
        if(rightBoundary > n) rightBoundary = n;
        if(bottomBoundary > n) bottomBoundary = n;

        /* Translate graphics so that entire map is visible and scrolling is smooth */
        graphics.translate(-viewportX, -viewportY);

        System.out.println("Left, Right: " + leftBoundary + " "  + rightBoundary);
        System.out.println("Top, Down: " + topBoundary + " "  + bottomBoundary);
        System.out.println("Viewport x, y: " + viewportX + " " + viewportY);
        System.out.println();

        leftBoundary = topBoundary = 0;
        rightBoundary = bottomBoundary = 100;
        for(int i = leftBoundary; i < rightBoundary; i++) {
            for(int j = topBoundary; j < bottomBoundary; j++) {
                graphics.drawImage(terrain[i][j] == "dirt" ? dirt : grass, (i)*tilesize, (j)*tilesize, null);
            }
        }
    }

}
