package com.scriptrts.core;

import jargs.gnu.CmdLineParser;

import java.awt.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;
import javax.swing.JPanel;

import com.scriptrts.game.*;
import com.scriptrts.util.*;
import com.scriptrts.script.*;
import com.scriptrts.control.Selection;

public class Main extends JPanel {
    /* Game properties */
    private static final int n = 129; //1 more than a power of 2
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

    /* Unit temp. controlled */
    SimpleUnit unit;

    /* Use fullscreen? */
    private static boolean fullscreen =  false;

    /* Debug mode? */
    private static boolean DEBUG = false;

    /* Use FPS logging or fixed FPS? */
    private static boolean fpsLogging = false;

    /* Input manager */
    private InputManager manager = InputManager.getInputManager();

    /* Create a new JPanel Main object with double buffering enabled */
    public Main() {
        super(true);
        setLayout(null);
    }

    private static void parseOptions(String[] args){
        /* Create parser */
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option debugOpt = parser.addBooleanOption('d', "debug");
        CmdLineParser.Option fullscreenOpt = parser.addBooleanOption('f', "fullscreen");
        CmdLineParser.Option fpsLogOpt = parser.addBooleanOption('l', "logfps");
        CmdLineParser.Option noMapDebugOpt = parser.addBooleanOption("nomapdebug");
        CmdLineParser.Option noUnitDebugOpt = parser.addBooleanOption("nounitdebug");
        CmdLineParser.Option noMaskOpt = parser.addBooleanOption("nomasking");

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
        fpsLogging = (Boolean) parser.getOptionValue(fpsLogOpt,  Boolean.FALSE);
        MapPainter.NO_MASKING = (Boolean) parser.getOptionValue(noMaskOpt,  Boolean.FALSE);

        boolean noMap = (Boolean) parser.getOptionValue(noMapDebugOpt,  Boolean.FALSE);
        boolean noUnit = (Boolean) parser.getOptionValue(noUnitDebugOpt,  Boolean.FALSE);

        MapPainter.DEBUG = DEBUG;
        UnitPainter.DEBUG = DEBUG;

        if(noMap)
            MapPainter.DEBUG = false;
        if(noUnit)
            UnitPainter.DEBUG = false;
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
        float fps = Main.getFPS();
        final TimerTask updateTask = new TimerTask(){
            public void run(){
                panel.updateGame();
                panel.repaint();
            }
        };
        Timer timer = new Timer();

        if(fpsLogging){
            new Thread(){
                public void run(){
                    while(true){
                        updateTask.run();
                    }
                }
            }.start();
        }
        else
            timer.scheduleAtFixedRate(updateTask, 0, (long) (1000 / fps));
    }

    /**
     * Get the frames per second (FPS) that this game should run at
     * @return fps number of frames and updates per second
     */
    public static int getFPS(){
        return 30;
    }

    /* Called when the window or drawing panel has changed size */
    public void resized(){
        if(viewport != null)
            viewport.resize(getWidth(), getHeight());
        if(console != null){
            remove(console);
            window.requestFocusInWindow();

            console.updateSize((int) (.30 * viewport.getHeight()), viewport.getWidth());
            Dimension size = console.getPreferredSize();
            console.setBounds(0, 0, size.width, size.height);

            if(consoleDown){
                add(console);
                console.requestFocusInWindow();
            }
        }
        repaint();
    }

    /* Initialize game resources */
    public void initializeGame(){
        /* Create and populate map with tiles */
        Map randomMap = new Map(n, ResourceDensity.Medium);
        randomMap.generateMap(.7);
        map = randomMap;

        /* Initialize scripting engine */
        if(!Script.initialized()) {
            Script.initialize();
            Script.exec("import sys");
            Script.exec("sys.path.append('./src/python')");
            Script.exec("import init");
        }

        /* Create map painter */
        mapPainter = new MapPainter(randomMap, 128, 64);
        int tileX = mapPainter.getTileWidth();
        int tileY = mapPainter.getTileHeight();

        /* Create the unit grid and unit painter */
        unitGrid = new UnitGrid(n);
        unitPainter = new UnitPainter(unitGrid, mapPainter);

        /* Create the viewport */
        viewport = new Viewport();
        viewport.setDim(getWidth(), getHeight());
        int totalWidth = map.getN() * mapPainter.getTileWidth();
        int totalHeight = map.getN() * mapPainter.getTileHeight();
        int[] limitxPts = {
            0, totalWidth/2 - viewport.getWidth()/2, totalWidth - viewport.getWidth(), totalWidth/2 - viewport.getWidth()/2
        };
        int[] limityPts = {
            totalHeight/2 - viewport.getHeight()/2, 0, totalHeight/2 - viewport.getHeight()/2,  totalHeight - viewport.getHeight()
        };
        Polygon limitingPolygon = new Polygon(limitxPts, limityPts, 4);
        viewport.setMapSize(limitxPts[2] - limitxPts[0], limityPts[3] - limityPts[1]);
        viewport.setViewportLocationLimits(limitingPolygon);
        viewport.translate(totalWidth / 2, totalHeight / 2);

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
        manager.registerKeyCode(KeyEvent.VK_S);
        manager.registerKeyCode(KeyEvent.VK_W);
        manager.registerKeyCode(KeyEvent.VK_D);
        manager.registerKeyCode(KeyEvent.VK_F11);

        /* Done with initialization */
        initialized = true;
    }

    /* Update game state */
    private Point topLeftSelection = null;
    private Point bottomRightSelection = null;
    private boolean prev = false;
    private double totalZoom = 1;
    boolean placingUnit = false;
    SimpleUnit tempUnit = null;
    int tempUnitX, tempUnitY;
    boolean consoleDown = false;
    Console console = null;

    public void updateGame(){
        /* Calling the console */
        if(manager.getKeyCodeFlag(KeyEvent.VK_F11)){
            manager.clearKeyCodeFlag(KeyEvent.VK_F11);

            /* Show or unshow the console */
            consoleDown = !consoleDown;
            if(consoleDown){
                add(console);
                console.requestFocusInWindow();
            } else {
                remove(console);
                window.requestFocusInWindow();
            }
        }

        if(console == null){
            console = new Console((int) (.30 * viewport.getHeight()), viewport.getWidth());
            console.addKeyListener(manager);
            Dimension size = console.getPreferredSize();
            console.setBounds(0, 0, size.width, size.height);
        }

        /* Disable map movements and actions when the console has focus */
        if(!console.hasFocus()){

            if(manager.getKeyCodeFlag(KeyEvent.VK_D) || manager.getKeyCodeFlag(KeyEvent.VK_S)){
                placingUnit = !placingUnit;
                int uSpeed;
                if(manager.getKeyCodeFlag(KeyEvent.VK_D))
                    uSpeed = 0;
                else
                    uSpeed = 1;

                manager.clearKeyCodeFlag(KeyEvent.VK_S);
                manager.clearKeyCodeFlag(KeyEvent.VK_D);

                Point point = manager.getMouseLocation();
                Point unitTile = unitPainter.unitTileAtPoint(point, viewport);
                tempUnitX = point.x;
                tempUnitY = point.y;

                try {
                    /* Retrieve spaceship sprites */
                    Sprite[] sprites = new Sprite[8];
                    for(Direction d : Direction.values()){
                        BufferedImage img = ResourceManager.loadImage("resource/unit/spaceship/Ship" + d.name() + ".png");
                        sprites[d.ordinal()]  = new Sprite(img, 0.3 * totalZoom, 87, 25);
                    }

                    SimpleUnit spaceship = new SimpleUnit(sprites, uSpeed, 0, 0, Direction.East, true);
                    tempUnit = spaceship;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(manager.getLeftMouseDown() && !prev){
                /* Get mouse location */
                Point point = manager.getMouseLocation();
                topLeftSelection = point;

            }

            if(placingUnit && manager.getMouseMoved()){
                Point point = manager.getMouseLocation();
                tempUnitX = point.x;
                tempUnitY = point.y;
            }

            /* Mouse released, but was never dragged */
            if(!manager.getLeftMouseDown() && prev && bottomRightSelection == null){
                /* Get mouse location */
                Point point = manager.getMouseLocation();

                /* Adding units to map */
                if(placingUnit){
                    Point unitTile = unitPainter.unitTileAtPoint(point, viewport);
                    tempUnit.setX(unitTile.x);
                    tempUnit.setY(unitTile.y);
                    unitGrid.placeUnit(tempUnit, unitTile.x, unitTile.y);

                    placingUnit = false;
                }
                else{
                    SimpleUnit unit = unitPainter.getUnitAtPoint(point, viewport);
                    if(unit != null) {
                        /* If already selected and pressing control, deselect */
                        if(manager.getKeyCodeFlag(KeyEvent.VK_CONTROL)){
                            if(Selection.current().contains(unit)){
                                Selection.current().remove(unit);
                            } else {
                                Selection.current().add(unit);
                            }
                        }
                        else {
                            Selection.current().clear();
                            Selection.current().add(unit);
                        }
                    } else {
                        Selection.current().clear();
                    }
                }

            }

            if(manager.getMouseDragged() && topLeftSelection != null){
                Point point = manager.getMouseLocation();
                bottomRightSelection = point;

                SimpleUnit[] selectedUnits = unitPainter.getUnitsInRect(topLeftSelection, bottomRightSelection, viewport);

                if(!manager.getKeyCodeFlag(KeyEvent.VK_CONTROL)){
                    Selection.current().clear();
                }

                for(SimpleUnit unit : selectedUnits)
                    Selection.current().add(unit);
            } else if(!manager.getLeftMouseDown()){
                topLeftSelection = null;
                bottomRightSelection = null;
            }


            prev = manager.getLeftMouseDown();

            /* Clicking (to set unit destination) */
            if(manager.getRightMouseClicked()){
                Point point = manager.getMouseLocation();
                Point unitTile = unitPainter.unitTileAtPoint(point, viewport);
                for(SimpleUnit unit : Selection.current().getCollection()){
                    unit.setDestination(unitTile);
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

        unitPainter.update();
    }

    private long prevTime = System.currentTimeMillis();
    protected void paintComponent(Graphics g) {
        if(!Console.calibrated())
            Console.calibrateFont(g);
        if(!initialized) return;

        /* Record FPS */
        if(fpsLogging){
            long t = System.currentTimeMillis();
            long diff = t - prevTime;
            prevTime = t;
            if(Math.random() < .03){
                if(diff == 0) diff = 1;
                System.out.println("FPS: " + (1000/diff));
            }
        }

        /* Clear screen */
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 2000, 2000);

        /* Move over to the viewport location */
        Graphics2D graphics = (Graphics2D) g;
        g.translate(-viewport.getX(), -viewport.getY());

        /* Paint the map using the map painter */
        mapPainter.paintMap(graphics, viewport);

        /* Paint the destination of all current selected units, if they share one */
        if(Selection.current().getCollection().size() != 0){
            /* Check if units share a destination */
            boolean shareDestination = true;
            Point destination = null;
            for(SimpleUnit unit : Selection.current().getCollection()){
                if(destination == null)
                    destination = unit.getDestination();
                else
                    if(!destination.equals(unit.getDestination())){
                        shareDestination = false;
                        break;
                    }
            }

            if(shareDestination && destination != null){
                unitPainter.paintDestination(graphics, destination.x, destination.y);
            }
        }

        /* On top of the map, paint all the units and buildings */
        unitPainter.paintUnits(graphics, viewport);

        /* Draw fake units and buildings on the board */
        if(placingUnit)
            drawTemporaryUnits(graphics, viewport);

        /* Draw selection (if not placing units) */
        else    
            drawSelection(graphics);

        /* From now on, paint in screen coordinates again */
        g.translate(viewport.getX(), viewport.getY());

        /* User interface */
        drawInterface(graphics);
    }

    private void drawSelection(Graphics2D graphics){
        if(topLeftSelection != null && bottomRightSelection != null){
            Point topLeft = new Point(topLeftSelection);
            Point bottomRight = new Point(bottomRightSelection);
            if(topLeft.x > bottomRight.x){
                int temp = topLeft.x;
                topLeft.x = bottomRight.x;
                bottomRight.x = temp;
            }
            if(topLeft.y > bottomRight.y){
                int temp = topLeft.y;
                topLeft.y = bottomRight.y;
                bottomRight.y = temp;
            }

            graphics.translate(viewport.getX(), viewport.getY());
            Color transparentBlue = new Color(0, 0, 255, 120);
            graphics.setColor(transparentBlue);
            graphics.fillRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
            graphics.setColor(Color.BLUE);
            graphics.drawRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
        }
    }

    private void drawTemporaryUnits(Graphics2D graphics, Viewport viewport){
        unitPainter.paintTemporaryUnit(graphics, viewport, tempUnit, tempUnitX, tempUnitY);
    }

    private void drawInterface(Graphics2D graphics){
    }
}
