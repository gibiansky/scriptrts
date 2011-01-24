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
import java.awt.Image;
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
    final static int n = 128, tileX = 128, tileY = 64;
    final static JFrame window = new JFrame("ScriptRTS");

    /* Viewport properties */
    static int width=800, height=600;
    int viewportX = tileX / 2, viewportY = tileY / 2;

    /* Game data */
    int[][] terrain;
    BufferedImage dirt, grass, maskTL;
    boolean initialized = false;
    MapPainter mapPainter;

    /* Input manager */
    InputManager manager = InputManager.getInputManager();

    /* Create a new JPanel Main object with double buffering enabled */
    public Main() {
        super(true);
    }

    public static void main(String... args) {
        /* Create window of default size */
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(width, height);

        /* Check for fullscreen */
        //boolean fullscreen = JOptionPane.showConfirmDialog(null, "Enable Full Screen display?", "Fullscreen?", JOptionPane.YES_NO_OPTION) == 0;

        boolean fullscreen = false;
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
        terrain = new int[n][n];
        for(int i=0; i<n; i++) {
            for(int j=0; j<n; j++) {
                if(Math.random() < 0.5)
                    terrain[i][j] = 0;
                else
                    terrain[i][j] = 1;
            }
        }

        // load textures
        dirt = grass = null;
        try {
            dirt = ImageIO.read(new File("resource/map/Martian Range.png"));
            grass = ImageIO.read(new File("resource/map/Silver Hex.png"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        /* Create map painter */
        BufferedImage[] imgs = {dirt, grass};
        mapPainter = new MapPainter(terrain, imgs);

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
        int increment = 30;
        if(manager.getKeyCodeFlag(KeyEvent.VK_RIGHT)) {
            viewportX += increment;
            if(viewportX >= n * tileX - width) viewportX = n * tileX - width;
        }
        if(manager.getKeyCodeFlag(KeyEvent.VK_LEFT)) {
            viewportX -= increment;
            if(viewportX <= tileX/2) viewportX = tileX / 2;
        }
        if(manager.getKeyCodeFlag(KeyEvent.VK_UP)) {
            viewportY -= increment;
            if(viewportY <= tileY/2) viewportY = tileY / 2;
        }
        if(manager.getKeyCodeFlag(KeyEvent.VK_DOWN)) {
            viewportY += increment;
            if(viewportY >= n * tileY/2 - height) viewportY = n * tileY/2 - height;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if(!initialized) return;

        /* Create a transformed graphics object to draw in perspective */
        Graphics2D graphics = (Graphics2D) g;
        g.translate(-viewportX, -viewportY);

        int leftBoundary = (int) (viewportX / tileX) - 1, topBoundary = (int) (viewportY / (tileY / 2)) - 1;
        if(leftBoundary < 0) leftBoundary = 0;
        if(topBoundary < 0) topBoundary = 0;

        int rightBoundary = (int) ((viewportX + width) / tileX) + 1, bottomBoundary = (int) ((viewportY + height) / (tileY / 2)) + 1;
        if(rightBoundary > n) rightBoundary = n;
        if(bottomBoundary > n) bottomBoundary = n;

        /* Paint the map using the map painter */
        mapPainter.paintMap(graphics, leftBoundary, topBoundary, rightBoundary - leftBoundary, bottomBoundary - topBoundary, tileX, tileY);
    }

}
