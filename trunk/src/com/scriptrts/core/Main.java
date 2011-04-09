package com.scriptrts.core;

import jargs.gnu.CmdLineParser;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.scriptrts.core.ui.Console;
import com.scriptrts.core.ui.GameMenu;
import com.scriptrts.core.ui.ImageButton;
import com.scriptrts.core.ui.InputManager;
import com.scriptrts.core.ui.MapPainter;
import com.scriptrts.core.ui.OverlayPane;
import com.scriptrts.core.ui.TopBar;
import com.scriptrts.core.ui.UnitPainter;
import com.scriptrts.game.Game;
import com.scriptrts.net.GameClient;
import com.scriptrts.net.GameServer;
import com.scriptrts.script.Script;
import com.scriptrts.util.ResourceManager;

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
    private final static int DEFAULT_WIDTH = 1024, DEFAULT_HEIGHT = 800;

    /**
     * Whether the game has already been initialized.
     */
    private boolean initialized = false;

    /**
     * Whether the game is currently paused
     */
    private boolean paused = true;

    /**
     * Whether the game is showing the main menu
     */
    private boolean showingMainMenu = true;

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
    public static boolean FULLSCREEN =  false;

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
    private Console console = null;

    /**
     * Top bar used to display resources and menu bar items
     */
    private TopBar topBar = null;

    /**
     * Top bar used to display resources and menu bar items
     */
    private OverlayPane overlay = null;

    public static OverlayPane getOverlay() {
		return main.overlay;
	}

	/** 
     * Whether or not the console is currently displayed.
     */
    private boolean consoleDown = false;

    /**
     * Currently showing menu. Set to null when no menu is showing.
     */
    private GameMenu currentMenu = null;

    /** 
     * Whether or not the menu bar is currently displayed.
     */
    private boolean menuDown = true;

    /** 
     * Whether or not the overlay is currently displayed.
     */
    private boolean overlayUp = true;

    /**
     * Input manager used to deal with inputs throughout the application.
     */
    private InputManager manager = InputManager.getInputManager();

    /**
     * Main instance used to access game properties from Python and external parts of the program.
     */
    private static Main main;

    /**
     * Image used as a background before loading is complete
     */
    private static BufferedImage loadingImage = null;
    
    /**
     * Image used for cursor
     */
    private static Cursor cursor;

    /**
     * Current game instance.
     */
    private Game game;

    /**
     * Game server running the game
     */
    private static GameServer server;

    /**
     * Game client connecting to a server
     */
    private static GameClient client;

    /**
     * IP to connect to
     */
    private static String serverIP = null;

    /**
     * Create a new Main object
     */
    public Main() {
        /* Enable double buffering and use an absolute layout */
        super(true);
        setLayout(null);
        setPreferredSize(window.getSize()); // make Window.pack() pack to the right size
        main = this;
        

        /* Create game and server */
        game = new Game(128, window.getWidth(), window.getHeight());
        game.init();
        if(serverIP == null){
            server = new GameServer();
            client = null;
            server.start(game);
        } else {
            server = null;
            client = new GameClient(serverIP);
        }
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

        /* If the screen is smaller or equal to default size, use full screen */
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
        if(screenSize.width <= DEFAULT_WIDTH && screenSize.height <= DEFAULT_HEIGHT)
            FULLSCREEN = true;

        /* Check for fullscreen */
        if(FULLSCREEN){
            /* Disable resizing and decorations */
            window.setUndecorated(true);

            /* Switch to fullscreen and make window maximum size */
            width = screenSize.width;
            height = screenSize.height;
            window.setSize(width, height);

            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            device.setFullScreenWindow(window);
        }

        /* Add graphics location */
        final Main panel = new Main();
        window.getContentPane().add(panel);
        
        /* Pack window to ensure actual usable area is the right size (ignoring look and feel) */
        window.pack();
        
        /* Load cursor */
        try {
            BufferedImage cursorImage = ResourceManager.loadImage("resource/cursor.png");
            Point hotSpot = new Point(0,0); 
            cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "cursor"); 
            window.setCursor(cursor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Load the loading image */
        try {
            loadingImage = ResourceManager.loadImage("resource/LoadingBackground.jpg");
        } catch(Exception e){
            e.printStackTrace();
        }

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

        /* Hide game components */
        if(panel.overlayUp)
            panel.overlay.setVisible(false);
        if(panel.consoleDown)
            panel.console.setVisible(false);
        if(panel.menuDown)
            panel.topBar.setVisible(false);

        /* Show the menu */
        GameMenu mainMenu = createMainMenu();
        mainMenu.setOpacity(0.0f);
        panel.showingMainMenu = true;
        panel.showMenu(mainMenu);

        /* Fade in the menu */
        /*
        for(int i = 0; i < 100; i++){
            panel.currentMenu.setOpacity((float)(i) / 100);
            try {
                Thread.sleep(10);
            } catch (Exception e){
                e.printStackTrace();
            }
            window.repaint();
        }
        panel.currentMenu.setOpacity(1.0f);
        */
        panel.startGame();

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
        CmdLineParser.Option fogOfWarOpt = parser.addBooleanOption('w', "fog-of-war");
        CmdLineParser.Option fullscreenOpt = parser.addBooleanOption('f', "fullscreen");
        CmdLineParser.Option fpsLogOpt = parser.addBooleanOption('l', "logfps");
        CmdLineParser.Option noMapDebugOpt = parser.addBooleanOption("nomapdebug");
        CmdLineParser.Option noUnitDebugOpt = parser.addBooleanOption("nounitdebug");
        CmdLineParser.Option noMaskOpt = parser.addBooleanOption("nomasking");
        CmdLineParser.Option noScriptOpt = parser.addBooleanOption('n', "disable-jython");

        /* Script options */
        CmdLineParser.Option scriptOpt = parser.addStringOption('m', "module");
        CmdLineParser.Option pathOpt = parser.addStringOption('p', "path");
        CmdLineParser.Option exprOpt = parser.addStringOption('e', "eval");

        /* Multiplayer options */
        CmdLineParser.Option ipOpt = parser.addStringOption('s', "server");

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


        serverIP = (String) parser.getOptionValue(ipOpt);

        /* Interpret arguments */
        DEBUG = (Boolean) parser.getOptionValue(debugOpt, Boolean.FALSE);
        FULLSCREEN = (Boolean) parser.getOptionValue(fullscreenOpt, Boolean.FALSE);
        fpsLogging = (Boolean) parser.getOptionValue(fpsLogOpt,  Boolean.FALSE);
        Script.DISABLE = (Boolean) parser.getOptionValue(noScriptOpt,  Boolean.FALSE);
        MapPainter.NO_MASKING = (Boolean) parser.getOptionValue(noMaskOpt,  Boolean.FALSE);
        MapPainter.USE_FOG_OF_WAR = (Boolean) parser.getOptionValue(fogOfWarOpt, Boolean.FALSE);

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
     * Create the game main menu
     * @return main menu object
     */
    private static GameMenu createMainMenu(){
        try {
            ImageButton[] buttons = new ImageButton[]{
                new ImageButton(
                        ResourceManager.loadImage("resource/StartGameButton.png"),
                        ResourceManager.loadImage("resource/StartGameButtonHighlighted.png"),
                        ResourceManager.loadImage("resource/StartGameButtonDown.png"),
                        .7, 0, 0
                        )

            };

            buttons[0].addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    Main.getMain().startGame();
                }
            });

            return new GameMenu(buttons, 700);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Show the main menu.
     */
    public void showMainMenu(){
        showingMainMenu = true;
        showMenu(createMainMenu());

        if(overlayUp)
            overlay.setVisible(false);
        if(consoleDown)
            console.setVisible(false);
        if(menuDown)
            topBar.setVisible(false);

        repaint();
    }

    /**
     * Return current game instance.
     * @return current game object
     */
    public static Game getGame(){
        return main.game;
    }

    /**
     * Return game client
     * @return client to the server
     */
    public static GameClient getGameClient(){
        return client;
    }

    /**
     * Get the game server
     * @return game server instance
     */
    public static GameServer getGameServer(){
        return server;
    }

    /**
     * Return current main instance.
     * @return current main object
     */
    public static Main getMain(){
        return main;
    }

    /**
     * Get whether the game is paused
     * @return true if game is paused, false otherwise
     */
    public boolean paused() {
        return paused;
    }

    /**
     * Pause the game
     */
    public void pause() {
        paused = true;
    }

    /**
     * Resume the game after a pause
     */
    public void unpause() {
        paused = false;
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
        main.setPreferredSize(getSize());

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

        /* Resize the showing menu, if there is one */
        if(currentMenu != null){
            remove(currentMenu);
            int centerMenu = currentMenu.getCenterHorizontalOffset();
            int centerScreen = getGame().getViewport().getWidth() / 2;

            int x = centerScreen - centerMenu;
            int y = 40;

            Dimension size = currentMenu.getPreferredSize();
            currentMenu.setBounds(x, y, size.width, size.height);
            add(currentMenu);
        }
        
        /* Update window size */
        window.pack();
        
        /* Redraw window after resize to fill in spaces which used to be blank */
        repaint();
    }

    /**
     * Initialize game resources 
     */
    public void initializeGame(){
        while(!Console.calibrated()){
            try { Thread.sleep(10); }
            catch (Exception e) {}
        }
        /* Initialize scripting engine */
        if(!Script.initialized() && !Script.DISABLE) {
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
            
            repaint();
        }

        /* Set up listeners */
        window.addKeyListener(manager);
        addKeyListener(manager);
        addMouseMotionListener(manager);
        addMouseListener(manager);
        addMouseWheelListener(manager);
        manager.registerKeyCode(KeyEvent.VK_F9);
        manager.registerKeyCode(KeyEvent.VK_F8);
        manager.registerKeyCode(KeyEvent.VK_F7);

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

        /* Done with initialization */
        initialized = true;
    }

    /**
     * Request focus back to main panel
     * @return true for success
     */
    public void requestFocus(){
        super.requestFocus();
        window.requestFocus();
    }

    /**
     * Start the game (leave the main menu)
     */
    public void startGame(){
        showingMainMenu = false;
        showMenu(null);


        topBar.setVisible(true);
        overlay.setVisible(true);
        console.setVisible(true);
    }

    /**
     * Show the provided menu on the screen
     * @param menu menu to show
     */
    public void showMenu(GameMenu menu){
        /* Remove old menu */
        if(currentMenu != null)
            remove(currentMenu);

        /* Update current menu */
        currentMenu = menu;

        /* Add menu to screen */
        if(menu != null){
            int centerMenu = menu.getCenterHorizontalOffset();
            int centerScreen = getGame().getViewport().getWidth() / 2;

            int x = centerScreen - centerMenu;
            int y = 40;

            Dimension size = menu.getPreferredSize();
            menu.setBounds(x, y, size.width, size.height);
            add(menu);

            pause();
        } else
            unpause();
    }

    /**
     * Update the state of the game.
     */
    public void updateGame(){

        if(!showingMainMenu){
            /* Calling the console */
            if(manager.getKeyCodeFlag(KeyEvent.VK_F9)){
                manager.clearKeyCodeFlag(KeyEvent.VK_F9);

                /* Show or unshow the console */
                consoleDown = !consoleDown;
                if(consoleDown){
                    add(console);
                    console.requestFocus();
                } else {
                    remove(console);
                    window.requestFocusInWindow();
                }
            }

            /* Calling the top bar */
            if(manager.getKeyCodeFlag(KeyEvent.VK_F8)){
                manager.clearKeyCodeFlag(KeyEvent.VK_F8);

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
            if(manager.getKeyCodeFlag(KeyEvent.VK_F7)){
                manager.clearKeyCodeFlag(KeyEvent.VK_F7);

                /* Show or unshow the console */
                overlayUp = !overlayUp;
                if(overlayUp){
                    add(overlay);
                } else {
                    remove(overlay);
                }
            }
        }

        /* Update the game */
        if(!paused){
            boolean inOverlay = overlay.isVisible() && overlay.getBounds().contains(manager.getMouseLocation());
            boolean inTopBar = topBar.isVisible() && topBar.getBounds().getBounds().contains(manager.getMouseLocation());
            game.handleInput(!console.hasFocus(), !inOverlay && !inTopBar);
        }
        game.update();
    }

    /**
     * Draw the game onto the screen.
     * @param g Graphics object which allows the program to draw on the panel.
     */
    protected void paintComponent(Graphics g) {
        /* Calibrate the console if necessary */
        if(!Console.calibrated())
            Console.calibrateFont(g);

        /* Until initialization is done, just show the loading image */
        if(!initialized || showingMainMenu) {
            g.drawImage(loadingImage, 0, 0, window.getSize().width, window.getSize().height, null);
            return;
        }

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
