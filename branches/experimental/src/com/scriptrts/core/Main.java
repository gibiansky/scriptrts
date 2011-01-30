package com.scriptrts.core;

import jargs.gnu.CmdLineParser;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.scriptrts.game.SimpleUnit;
import com.scriptrts.game.SpriteState;
import com.scriptrts.game.UnitGrid;

public class Main extends JPanel {
	/* Game properties */
	private static final int n = 32;
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
	}

	private static void parseOptions(String[] args){
		/* Create parser */
		CmdLineParser parser = new CmdLineParser();
		CmdLineParser.Option debugOpt = parser.addBooleanOption('d', "debug");
		CmdLineParser.Option fullscreenOpt = parser.addBooleanOption('f', "fullscreen");
		CmdLineParser.Option fpsLogOpt = parser.addBooleanOption('l', "logfps");

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

		/* Retrieve the unit we will be controlling */
		unit = unitGrid.unitGrid[5][5].units[UnitLocation.Center.ordinal()];

		/* Create the viewport */
		viewport = new Viewport(getWidth(), getHeight(), map.getN() * mapPainter.getTileWidth(), map.getN() * mapPainter.getTileHeight());
        int totalWidth = map.getN() * mapPainter.getTileWidth();
        int totalHeight = map.getN() * mapPainter.getTileHeight();
		viewport.translate(totalWidth / 2, totalHeight / 2);
        int[] limitxPts = {0, totalWidth/2 - viewport.getWidth()/2, totalWidth - viewport.getWidth(), totalWidth/2 - viewport.getWidth()/2};
        int[] limityPts = {totalHeight/2 - viewport.getHeight()/2, 0, totalHeight/2 - viewport.getHeight()/2,  totalHeight - viewport.getHeight()};
        Polygon limitingPolygon = new Polygon(limitxPts, limityPts, 4);
		viewport.setViewportLocationLimits(limitingPolygon);

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
		manager.registerKeyCode(KeyEvent.VK_W);

		/* Done with initialization */
		initialized = true;
	}

	/* Update game state */
    private Point topLeftSelection = null;
    private Point bottomRightSelection = null;
    private boolean prev = false;
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


            /* Paint on the map */
			if(manager.getKeyCodeFlag(KeyEvent.VK_CONTROL)){
				TerrainType type = map.getTileArray()[tileLocY][tileLocX];
				paintbrush = type;
            } else {
                map.getTileArray()[tileLocY][tileLocX] = paintbrush;
                mapPainter.update();
            }
        }

        if(0 != 0){
            if(manager.getMouseDown() && !prev){
                /* Get mouse location */
                Point point = manager.getMouseLocation();
                topLeftSelection = point;
            }

            /*
               System.out.println("CLICK: " + clicked);
               System.out.println("DRAGGED: " + manager.getMouseDragged());
               System.out.println("DOWN: " + manager.getMouseDown());
               */

            if(manager.getMouseDragged() && topLeftSelection != null){
                Point point = manager.getMouseLocation();
                bottomRightSelection = point;

                SimpleUnit[] selectedUnits = unitPainter.getUnitsInRect(topLeftSelection, bottomRightSelection, viewport);

                for(SimpleUnit unit : selectedUnits){
                    if(!unit.isSelected())
                        unit.select();
                }
            } else if(!manager.getMouseDown()){
                topLeftSelection = null;
                bottomRightSelection = null;
            }

            prev = manager.getMouseDown();
        }

		/* Detect unit commands */
		if(manager.getKeyCodeFlag(KeyEvent.VK_W)) {
			unit.move();
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

			/* Make sure map isn't smaller than screen at new zoom level */
			if(newTileX * map.getN() > viewport.getWidth() && newTileY / 2 * map.getN() > viewport.getHeight())
				/* Try to resize the tiles */
				if(mapPainter.setTileSize(newTileX, newTileY)){
					/* Prevent the viewport from going off the map */
                    int totalWidth = map.getN() * mapPainter.getTileWidth();
                    int totalHeight = map.getN() * mapPainter.getTileHeight();
                    int[] limitxPts = {0, totalWidth/2 - viewport.getWidth()/2, totalWidth - viewport.getWidth(), totalWidth/2 - viewport.getWidth()/2};
                    int[] limityPts = {totalHeight/2 - viewport.getHeight()/2, 0, totalHeight/2 - viewport.getHeight()/2,  totalHeight - viewport.getHeight()};
                    Polygon limitingPolygon = new Polygon(limitxPts, limityPts, 4);
                    viewport.setViewportLocationLimits(limitingPolygon);
                    viewport.setMapSize(map.getN() * mapPainter.getTileWidth(), map.getN() * mapPainter.getTileHeight());
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

        unitPainter.update();
	}

	private long prevTime = System.currentTimeMillis();
	@Override
	protected void paintComponent(Graphics g) {
		if(!initialized) return;

		if(fpsLogging){
			long t = System.currentTimeMillis();
			long diff = t - prevTime;
			prevTime = t;
			if(Math.random() < .03){
				if(diff == 0) diff = 1;
				System.out.println("FPS: " + (1000/diff));
			}
		}

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 2000, 2000);

		/* Move over to the viewport location */
		Graphics2D graphics = (Graphics2D) g;
		g.translate(-viewport.getX(), -viewport.getY());

		/* Paint the map using the map painter */
		mapPainter.paintMap(graphics, viewport);

		/* On top of the map, paint all the units and buildings */
		//unitPainter.paintUnits(graphics, viewport);

        /* Draw selection */
        drawSelection(graphics);
	}

    private void drawSelection(Graphics2D graphics){
        if(topLeftSelection != null && bottomRightSelection != null){
            graphics.translate(viewport.getX(), viewport.getY());
            Color transparentBlue = new Color(0, 0, 255, 120);
            graphics.setColor(transparentBlue);
            graphics.fillRect(topLeftSelection.x, topLeftSelection.y, bottomRightSelection.x - topLeftSelection.x, bottomRightSelection.y - topLeftSelection.y);
            graphics.setColor(Color.BLUE);
            graphics.drawRect(topLeftSelection.x, topLeftSelection.y, bottomRightSelection.x - topLeftSelection.x, bottomRightSelection.y - topLeftSelection.y);
        }
    }
}
