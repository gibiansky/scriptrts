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

import com.scriptrts.game.*;

public class Main extends JPanel {
    /* Game properties */
    private static final int n = 128;
    private final static JFrame window = new JFrame("ScriptRTS");

    /* Viewport properties */
    private Viewport viewport;
    private final static int DEFAULT_WIDTH = 800, DEFAULT_HEIGHT = 600;

    private TerrainType paintbrush = TerrainType.DeepFire;

    /* Game data */
    private boolean initialized = false;
    private MapPainter mapPainter;
    private UnitGrid unitGrid;
    private UnitPainter unitPainter;
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
        final TimerTask updateTask = new TimerTask(){
            public void run(){
                panel.updateGame();
                panel.repaint();
            }
        };
        Timer timer = new Timer();
        //timer.scheduleAtFixedRate(updateTask, 0, (long) (1000 / fps));
        new Thread(){
            public void run(){
                while(true){
                    updateTask.run();
                }
            }
        }.start();
    }

    /* Called when the window or drawing panel has changed size */
    public void resized(){
        if(viewport != null)
            viewport.resize(getWidth(), getHeight());
        repaint();
    }

    /* Initialize game resources */
    public void initializeGame(){
        /* Create and populate map with tiles */
        Map randomMap = new Map(n, ResourceDensity.Medium);
        randomMap.populateTiles();
        map = randomMap;

        /* Create map painter */
        mapPainter = new MapPainter(randomMap, 128, 64);
        int tileX = mapPainter.getTileWidth();
        int tileY = mapPainter.getTileHeight();

        /* Create the unit grid and unit painter */
        unitGrid = new UnitGrid(UnitPainter.SPACES_PER_TILE * n);
        unitPainter = new UnitPainter(unitGrid, mapPainter);

        /* Create the viewport */
        viewport = new Viewport(getWidth(), getHeight());
        viewport.translate(tileX / 2, tileY / 2);
        viewport.setViewportLocationLimits(tileX / 2, tileY / 2, n * tileX, n * tileY / 2);

        // set up key listeners
        window.addKeyListener(manager);
        addMouseMotionListener(manager);
        addMouseListener(manager);
        addMouseWheelListener(manager);
        manager.registerKeyCode(KeyEvent.VK_LEFT);
        manager.registerKeyCode(KeyEvent.VK_RIGHT);
        manager.registerKeyCode(KeyEvent.VK_UP);
        manager.registerKeyCode(KeyEvent.VK_DOWN);
        manager.registerKeyCode(KeyEvent.VK_CONTROL);

        /* Done with initialization */
        initialized = true;
    }

    /* Update game state */
    private long prevTime = System.nanoTime();
    public void updateGame(){
        /* Try to accept and locate mouse clicks */
        if(manager.getMouseDown() && manager.getMouseMoved()){
            /* Get mouse location */
            Point point = manager.getMouseLocation();

            /* Convert map location into absolute coordinates on the map */
            Point tileLoc = mapPainter.getTileAtPoint(point, viewport);
            if(tileLoc == null) return;

            int tileLocX = (int) tileLoc.getX();
            int tileLocY = (int) tileLoc.getY();

            if(manager.getKeyCodeFlag(KeyEvent.VK_CONTROL)){
                TerrainType type = map.getTileArray()[tileLocY][tileLocX];
                paintbrush = type;
            } else {
                map.getTileArray()[tileLocY][tileLocX] = paintbrush;
                mapPainter.update();
            }
        }

        /* Zooming */
        if(manager.getMouseScrolled()){
            int zoomLevel = -manager.getMouseScrollDistance();
            
            /* Calculate new tile sizes */
            int newTileX = mapPainter.getTileWidth(), newTileY = mapPainter.getTileHeight();
            if(zoomLevel > 0){
                newTileX *= 2;
                newTileY *= 2;
            } else if(zoomLevel < 0) {
                newTileX /= 2;
                newTileY /= 2;
            }

            /* Remember what we were looking at before */
            Point topLeft = mapPainter.getTileAtPoint(new Point(0, 0), viewport);

            /* Try to resize the tiles */
            if(mapPainter.setTileSize(newTileX, newTileY)){
                /* Prevent the viewport from going off the map */
                viewport.setViewportLocationLimits(newTileX / 2, newTileY / 2, n * newTileX, n * newTileY / 2);
                viewport.translate(0, 0);

                /* What are we looking at now? */
                Point topLeftUpdated = mapPainter.getTileAtPoint(new Point(0, 0), viewport);

                /* Shift back to what we were looking at before, approximately */
                viewport.translate((int)(topLeft.x - topLeftUpdated.x) * newTileX, (int)(topLeft.y - topLeftUpdated.y) * newTileY);
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

        long t = System.currentTimeMillis();
        long diff = t - prevTime;
        prevTime = t;
        if(Math.random() < .1)
            System.out.println("FPS " + (1000/diff));

        /* Move over to the viewport location */
        Graphics2D graphics = (Graphics2D) g;
        g.translate(-viewport.getX(), -viewport.getY());

        /* Paint the map using the map painter */
        mapPainter.paintMap(graphics, viewport);

        /* On top of the map, paint all the units and buildings */
        unitPainter.paintUnits(graphics, viewport);
    }

}
