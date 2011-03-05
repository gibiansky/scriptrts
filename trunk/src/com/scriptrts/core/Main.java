package com.scriptrts.core;

import jargs.gnu.CmdLineParser;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.scriptrts.control.Selection;
import com.scriptrts.control.SelectionStorage;
import com.scriptrts.script.Script;

/**
 * Main game class which starts the application, parses command line arguments, and initializes the game loop.
 */
public class Main extends JPanel {

    /**
     * Window used to display game.
     */
    private final static JFrame window = new JFrame("ScriptRTS");

    /**
     * Default size of the window.
     */
    private final static int DEFAULT_WIDTH = 800, DEFAULT_HEIGHT = 600;

    /**
     * Whether the game has already been initialized.
     */
    private boolean initialized = false;

    /**
     * Directories to add to the interpreter path.
     */
    private static ArrayList<String> pyPaths = new ArrayList<String>();

    /**
     * Modules to import after interpreter initialization.
     */
    private static ArrayList<String> pyScripts = new ArrayList<String>();

    /**
     * Expressions to evaluate after interpreter initialization and module importing.
     */
    private static ArrayList<String> pyExprs = new ArrayList<String>();

    /**
     * Whether to use fullscreen (or alternatively, a windowed mode)
     */
    private static boolean fullscreen =  false;

    /**
     * Whether to enable debug drawing and printing.
     */
    private static boolean DEBUG = false;

    /**
     * Whether to run the game at maximum FPS and occasionally print FPS. This helps us determine 
     * how fast our graphics are functioning and is useful for occasional optimisation.
     */
    private static boolean fpsLogging = false;

    /**
     * Counter used for logging FPS. Represents time when paint() was called.
     */
    private static long previousTime = System.currentTimeMillis();

    /**
     * Console used to enter commands during the game.
     */
    Console console = null;

    /**
     * Top bar used to display resources and menu bar items
     */
    TopBar topBar = null;

    /**
     * Top bar used to display resources and menu bar items
     */
    OverlayPane overlay = null;

    /** 
     * Whether or not the console is currently displayed.
     */
    boolean consoleDown = false;

    /** 
     * Whether or not the menu bar is currently displayed.
     */
    boolean menuDown = true;

    /** 
     * Whether or not the overlay is currently displayed.
     */
    boolean overlayUp = true;

    /**
     * Input manager used to deal with inputs throughout the application.
     */
    private InputManager manager = InputManager.getInputManager();

    /**
     * Main instance used to access game properties from Python and external parts of the program.
     */
    private static Main main;

    /**
     * Current game instance.
     */
    private Game game;

    /**
     * Create a new Main object
     */
    public Main() {
        /* Enable double buffering and use an absolute layout */
        super(true);
        setLayout(null);
        main = this;

        /* Create game */
        game = new Game(129, window.getWidth(), window.getHeight());
    }

    /**
     * Program entry point, initializes and starts program.
     * @param args list of command-line arguments.
     */
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

        /* If we want to log FPS, just run the program at max speed */
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
     * Parse the command-line options.
     * @param args list of command line arguments, as received by main()
     */
    private static void parseOptions(String[] args){
        /* Create parser */
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option debugOpt = parser.addBooleanOption('d', "debug");
        CmdLineParser.Option fullscreenOpt = parser.addBooleanOption('f', "fullscreen");
        CmdLineParser.Option fpsLogOpt = parser.addBooleanOption('l', "logfps");
        CmdLineParser.Option noMapDebugOpt = parser.addBooleanOption("nomapdebug");
        CmdLineParser.Option noUnitDebugOpt = parser.addBooleanOption("nounitdebug");
        CmdLineParser.Option noMaskOpt = parser.addBooleanOption("nomasking");

        /* Script options */
        CmdLineParser.Option scriptOpt = parser.addStringOption('m', "module");
        CmdLineParser.Option pathOpt = parser.addStringOption('p', "path");
        CmdLineParser.Option exprOpt = parser.addStringOption('e', "eval");

        /* Parse */
        try {
            parser.parse(args);
        }
        catch ( CmdLineParser.OptionException e ) {
            System.err.println(e.getMessage());
            System.exit(2);
        }

        /* Collect paths, scripts, expressions to plug in to the intepreter */
        while(true){
            String path = (String) parser.getOptionValue(pathOpt);
            if(path == null)
                break;
            else
                pyPaths.add(path);
        }
        while(true){
            String script = (String) parser.getOptionValue(scriptOpt);
            if(script == null)
                break;
            else
                pyScripts.add(script);
        }
        while(true){
            String expr = (String) parser.getOptionValue(exprOpt);
            if(expr == null)
                break;
            else
                pyExprs.add(expr);
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

        /* Allow user to turn off parts of the debug */
        if(noMap)
            MapPainter.DEBUG = false;
        if(noUnit)
            UnitPainter.DEBUG = false;
    }

    /**
     * Return current game instance.
     * @return current game object
     */
    public static Game getGame(){
        return main.game;
    }

    /**
     * Get the frames per second (FPS) that this game should run at
     * @return fps number of frames and updates per second
     */
    public static int getFPS(){
        return 30;
    }

    /**
     * Called when the window or drawing panel has changed size; resizes the window and any components inside.
     * */
    public void resized(){
        /* Resize viewport inside game */
        getGame().getViewport().resize(getWidth(), getHeight());

        /* Resize console */
        if(console != null){
            /* Remove console before resizing */
            remove(console);
            window.requestFocusInWindow();

            /* Update console size */
            console.updateSize((int) (.40 * getGame().getViewport().getHeight()), getGame().getViewport().getWidth());
            Dimension size = console.getPreferredSize();
            if(menuDown)
                console.setBounds(0, topBar.getPreferredSize().height, size.width, size.height);
            else
                console.setBounds(0, 0, size.width, size.height);

            /* Re-add console if it was down before */
            if(consoleDown){
                add(console);
                console.requestFocusInWindow();
            }
        }

        /* Resize the top bar */
        if(topBar != null){
            topBar.setWidth(getGame().getViewport().getWidth());
            Dimension size = topBar.getPreferredSize();
            topBar.setBounds(0, 0, size.width, size.height);
        }

        /* Resize the top bar */
        if(overlay != null){
            overlay.setWidth(getGame().getViewport().getWidth());
            Dimension size = overlay.getPreferredSize();
            overlay.setBounds(0, getGame().getViewport().getHeight() - size.height, size.width, size.height);
        }

        /* Redraw window after resize to fill in spaces which used to be blank */
        repaint();
    }

    /**
     * Initialize game resources 
     */
    public void initializeGame(){
        /* Initialize scripting engine */
        if(!Script.initialized()) {
            Script.initialize();
            Script.exec("import sys");
            Script.exec("sys.path.append('./src/python')");

            /* Add paths */
            for(String path : pyPaths)
                Script.exec("sys.path.append('" + path + "')");

            /* Our own initialization scripts */
            System.out.print(Script.exec("import core"));
            System.out.print(Script.exec("from lib import *"));

            String[] initModules = {"selection", "map"};
            for(String module : initModules)
                System.out.print(Script.exec("import " + module));


            /* Disable importing scriptrts classes */
            Script.exec("core.disallow_scriptrts_import()");

            /* Custom init scripts */
            for(String script : pyScripts)
                System.out.print(Script.exec("import " + script));

            /* Custom init expressions */
            for(String expr : pyExprs)
                System.out.print(Script.exec(expr));
        }

        /* Set up listeners */
        window.addKeyListener(manager);
        addMouseMotionListener(manager);
        addMouseListener(manager);
        addMouseWheelListener(manager);
        manager.registerKeyCode(KeyEvent.VK_F11);
        manager.registerKeyCode(KeyEvent.VK_F10);
        manager.registerKeyCode(KeyEvent.VK_F9);

        /* Initialize the top bar */
        topBar = new TopBar(getGame().getViewport().getWidth());
        Dimension size = topBar.getPreferredSize();
        topBar.setBounds(0, 0, size.width, size.height);
        add(topBar);

        /* Initialize the console if it hasn't been created */
        console = new Console((int) (.40 * getGame().getViewport().getHeight()), getGame().getViewport().getWidth());
        console.addKeyListener(manager);
        size = console.getPreferredSize();
        if(menuDown)
            console.setBounds(0, topBar.getPreferredSize().height, size.width, size.height);
        else
            console.setBounds(0, 0, size.width, size.height);

        /* Initialize the overlay */
        overlay = new OverlayPane(getGame().getViewport());
        overlay.setWidth(getGame().getViewport().getWidth());
        size = overlay.getPreferredSize();
        overlay.setBounds(0, getGame().getViewport().getHeight() - size.height, size.width, size.height);
        add(overlay);

        game.init();

        /* Done with initialization */
        initialized = true;
    }

    /**
     * Update the state of the game.
     */
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

        /* Calling the top bar */
        if(manager.getKeyCodeFlag(KeyEvent.VK_F10)){
            manager.clearKeyCodeFlag(KeyEvent.VK_F10);

            /* Show or unshow the console */
            menuDown = !menuDown;
            if(menuDown){
                add(topBar);
            } else {
                remove(topBar);
            }

            Dimension size = console.getPreferredSize();
            if(menuDown)
                console.setBounds(0, topBar.getPreferredSize().height, size.width, size.height);
            else
                console.setBounds(0, 0, size.width, size.height);
        }

        /* Calling the overlay */
        if(manager.getKeyCodeFlag(KeyEvent.VK_F9)){
            manager.clearKeyCodeFlag(KeyEvent.VK_F9);

            /* Show or unshow the console */
            overlayUp = !overlayUp;
            if(overlayUp){
                add(overlay);
            } else {
                remove(overlay);
            }
        }

        /* Update the game */
        game.update(!console.hasFocus());
    }

    /**
     * Draw the game onto the screen.
     * @param g Graphics object which allows the program to draw on the panel.
     */
    protected void paintComponent(Graphics g) {
        /* Calibrate the console if necessary */
        if(!Console.calibrated())
            Console.calibrateFont(g);

        /* Do not draw anything until initialization is done */
        if(!initialized) return;

        /* Record FPS if the option was enabled (via command-line switch) */
        if(fpsLogging){
            long currentTime = System.currentTimeMillis();
            long difference = currentTime - previousTime;
            previousTime = currentTime;

            /* Only print FPS occasionally, so printing doesn't influence measurement */
            if(Math.random() < .03){
                /* Avoid division by zero if the program is too fast or errors occur */
                if(difference == 0) difference = 1;

                /* Print FPS */
                System.out.println("FPS: " + (1000/difference));
            }
        }

        /* Paint the game */
        game.paint((Graphics2D) g);
    }
}
