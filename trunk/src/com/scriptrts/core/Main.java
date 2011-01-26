package com.scriptrts.core;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.awt.Point;
import java.awt.Polygon;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jargs.gnu.CmdLineParser;

public class Main extends JPanel {
    /* Game properties */
    private final static int n = 128, tileX = 128, tileY = 64;
    private final static JFrame window = new JFrame("ScriptRTS");

    /* Viewport properties */
    private Viewport viewport;

    private final static int DEFAULT_WIDTH = 800, DEFAULT_HEIGHT = 600;

    /* Game data */
    private boolean initialized = false;
    private MapPainter mapPainter;
    private Map map;

    /* Use fullscreen? */
    private static boolean fullscreen =  false;
    
    /* Debug mode? */
    private static boolean DEBUG = false;
    

    /* Input manager */
    private InputManager manager = InputManager.getInputManager();

    /* Create a new JPanel Main object with double buffering enabled */
    public Main() {
        super(true);
    }

    private static void parseOptions(String[] args){
        /* Create parser */
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option debugOpt = parser.addBooleanOption('d', "debug");
        CmdLineParser.Option fullscreenOpt = parser.addBooleanOption('f', "fullscreen");

        /* Parse */
        try {
            parser.parse(args);
        }
        catch ( CmdLineParser.OptionException e ) {
            System.err.println(e.getMessage());
            System.exit(2);
        }

        /* Interpret arguments */
        DEBUG = (Boolean) parser.getOptionValue(debugOpt, Boolean.FALSE);
        fullscreen = (Boolean) parser.getOptionValue(fullscreenOpt, Boolean.FALSE);

        MapPainter.DEBUG = DEBUG;
    }

    public static void main(String... args) {
        /* Parse command-line options */
        parseOptions(args);

        /* Create window of default size */
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(width, height);

        /* Check for fullscreen */
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

        /* Allow window resizing */
        panel.addComponentListener(new ComponentAdapter(){
            public void componentResized(ComponentEvent c){
                panel.resized();
            }
        });

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

    /* Called when the window or drawing panel has changed size */
    public void resized(){
        viewport.resize(getWidth(), getHeight());
        repaint();
    }

    /* Initialize game resources */
    public void initializeGame(){
        /* Create the viewport */
        viewport = new Viewport(getWidth(), getHeight());
        viewport.translate(tileX / 2, tileY / 2);
        viewport.setViewportLocationLimits(tileX / 2, tileY / 2, n * tileX, n * tileY / 2);

        /* Create and populate map with tiles */
        Map randomMap = new Map(n, ResourceDensity.Medium);
        randomMap.populateTiles();
        map = randomMap;

        /* Create map painter */
        mapPainter = new MapPainter(randomMap, tileX, tileY);

        // set up key listeners
        window.addKeyListener(manager);
        addMouseMotionListener(manager);
        addMouseListener(manager);
        manager.registerKeyCode(KeyEvent.VK_LEFT);
        manager.registerKeyCode(KeyEvent.VK_RIGHT);
        manager.registerKeyCode(KeyEvent.VK_UP);
        manager.registerKeyCode(KeyEvent.VK_DOWN);
        manager.registerKeyCode(KeyEvent.VK_CONTROL);

        /* Done with initialization */
        initialized = true;
    }

    /* Get the tile that is being clicked on */
    private Point getClickedTile(Point point){
        /* Convert map location into absolute coordinates on the map */
        point.translate(viewport.getX(), viewport.getY());

        int mouseX = (int) point.getX();
        int mouseY = (int) point.getY();

        int leftBoundary = (int) (viewport.getX() / tileX) - 1; 
        int topBoundary = (int) (viewport.getY() / (tileY / 2)) - 1;
        if(leftBoundary < 0) leftBoundary = 0;
        if(topBoundary < 0) topBoundary = 0;

        int rightBoundary = (int) ((viewport.getX() + viewport.getWidth()) / tileX) + 1;
        int bottomBoundary = (int) ((viewport.getY() + viewport.getHeight()) / (tileY / 2)) + 1;
        if(rightBoundary > n) rightBoundary = n;
        if(bottomBoundary > n) bottomBoundary = n;

        Polygon[][] visibleTiles = mapPainter.getVisibleTilePolygons(leftBoundary, topBoundary, rightBoundary - leftBoundary, bottomBoundary - topBoundary);

        int tileLocX = -1, tileLocY = -1;
        for(int i = 0; i < visibleTiles.length; i++){
            for(int j = 0; j < visibleTiles[0].length; j++){
                if(visibleTiles[i][j].contains(mouseX, mouseY)){
                    tileLocY = i + topBoundary;
                    tileLocX = j + leftBoundary;
                }
            }
        }

        return new Point(tileLocX, tileLocY);
    }

    /* Update game state */
    private TerrainType paintbrush = TerrainType.DeepFire;
    public void updateGame(){
        /* Try to accept and locate mouse clicks */
        if(manager.getMouseDown() && manager.getMouseMoved()){
            /* Get mouse location */
            Point point = manager.getMouseLocation();
            Point tileLoc = getClickedTile(point);
            int tileLocX = (int) tileLoc.getX();
            int tileLocY = (int) tileLoc.getY();

            if(tileLocX < 0 || tileLocY < 0) return;
            if(tileLocX >= n || tileLocY >= n) return;

            if(manager.getKeyCodeFlag(KeyEvent.VK_CONTROL)){
                TerrainType type = map.getTileArray()[tileLocY][tileLocX];
                paintbrush = type;
            } else {
                map.getTileArray()[tileLocY][tileLocX] = paintbrush;
                mapPainter.update();
            }
        }

        /* Scrolling */
        int increment = 30;
        if(manager.getKeyCodeFlag(KeyEvent.VK_RIGHT)) 
            viewport.translate(increment, 0);
        if(manager.getKeyCodeFlag(KeyEvent.VK_LEFT))
            viewport.translate(-increment, 0);
        if(manager.getKeyCodeFlag(KeyEvent.VK_UP))
            viewport.translate(0, -increment);
        if(manager.getKeyCodeFlag(KeyEvent.VK_DOWN))
            viewport.translate(0, increment);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if(!initialized) return;

        /* Create a transformed graphics object to draw in perspective */
        Graphics2D graphics = (Graphics2D) g;
        g.translate(-viewport.getX(), -viewport.getY());

        int leftBoundary = (int) (viewport.getX() / tileX) - 1;
        int topBoundary = (int) (viewport.getY() / (tileY / 2)) - 1;
        if(leftBoundary < 0) leftBoundary = 0;
        if(topBoundary < 0) topBoundary = 0;

        int rightBoundary = (int) ((viewport.getX() + viewport.getWidth()) / tileX) + 1;
        int bottomBoundary = (int) ((viewport.getY() + viewport.getHeight()) / (tileY / 2)) + 1;
        if(rightBoundary > n) rightBoundary = n;
        if(bottomBoundary > n) bottomBoundary = n;

        /* Paint the map using the map painter */
        mapPainter.paintMap(graphics, leftBoundary, topBoundary, rightBoundary - leftBoundary, bottomBoundary - topBoundary);

    }

}
