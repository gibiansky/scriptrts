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
    static int n = 1280, tilesize = 32, width=800, height=600;
    String[][] terrain;
    BufferedImage dirt, grass;
    BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D ig = (Graphics2D) buf.getGraphics();
    InputManager manager = InputManager.getInputManager();

    /* Create a new JPanel Main object with double buffering enabled */
    public Main() {
        super(true);
    }

    public static void main(String... args) {
        /* Create window of default size */
        final JFrame window = new JFrame("ScriptRTS");
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
    }

    /* Update game state */
    public void updateGame(){

    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics = (Graphics2D) g;
        int leftBoundary = 0, topBoundary = 0;
        for(int i = leftBoundary; i < leftBoundary + width/tilesize + 1; i++) {
            for(int j = topBoundary; j < topBoundary + height/tilesize + 1; j++) {
                graphics.drawImage(terrain[i][j] == "dirt" ? dirt : grass, (i - leftBoundary)*tilesize, (j - topBoundary)*tilesize, null);
            }
        }
    }

}
