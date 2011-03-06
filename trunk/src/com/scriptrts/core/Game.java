package com.scriptrts.core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import com.scriptrts.control.Selection;
import com.scriptrts.control.SelectionStorage;
import com.scriptrts.game.Direction;
import com.scriptrts.game.SimpleUnit;
import com.scriptrts.game.Player;
import com.scriptrts.game.Sprite;
import com.scriptrts.game.UnitGrid;
import com.scriptrts.util.ResourceManager;

/**
 * Game class which stores all information about a currently running game and manages
 * updating and drawing of the map and game features.
 */
public class Game {

    /**
     * Viewport used to display the map on the scren.
     */ 
    private Viewport viewport;

    /**
     * The map on which this game is being played
     */
    private Map map;

    /**
     * Size of the map (length along one edge). This is measured in map tiles, not unit tiles.
     * Each map tile has some number of unit tiles in it (3 as of last check).
     */
    private int n; 

    /**
     * Input manager used to deal with inputs throughout the application.
     */
    private InputManager manager = InputManager.getInputManager();

    /**
     * The grid of units on the board.
     */
    private UnitGrid unitGrid;

    /**
     * The painter used to draw the map onto the screen.
     */
    private MapPainter mapPainter;

    /**
     * The painter used to draw units onto the screen.
     */
    private UnitPainter unitPainter;

    /**
     * The current player
     */
    private Player player;

    /**
     * Top point of the current selection on the map. If this is null, it means there is no selection.
     */
    private Point topLeftSelection = null;

    /**
     * Bottom point of the current selection on the map. If this is null, it means there is no selection.
     */
    private Point bottomRightSelection = null;

    /** 
     * Whether the user is currently placing a unit.
     */
    private boolean placingUnit = false;

    /**
     * The unit currently being placed on the map, if a unit is being placed. It is drawn over the map,
     * and, unlike other units on the map, is not necessarily placed on the grid.
     */
    private SimpleUnit tempUnit = null;

    /**
     * Location (in screen coordinates) at which the unit being placed is being placed.
     */
    private int tempUnitX, tempUnitY;

    /**
     * Whether the mouse was pressed on last update.
     */
    private boolean mousePreviouslyPressed = false;


    /**
     * Create the game.
     * @param n size of the map (length along one edge)
     */
    public Game(int n, int width, int height) {
        super();

        /* Store map size */
        this.n = n;

        /* Initialize viewport */
        viewport = new Viewport();
        viewport.setDim(width,  height);
    }

    /**
     * Initialize this game.
     */
    public void init(){
        /* Create the player */
        player = new Player("Player-One", Color.MAGENTA);

        /* Create and populate map with tiles */
        map = new Map(n);
        map.generateMap(.64);
        
        /* Test pathfinder **********REMOVE THIS LATER********** */
        Pathfinder3 pathfinder = new Pathfinder3(null, map, null);
        pathfinder.findRoute(0,0,120,120);
        System.out.println(pathfinder.getDirections());

        /* Create map painter */
        mapPainter = new MapPainter(map, 128, 64);
        int tileX = mapPainter.getTileWidth();
        int tileY = mapPainter.getTileHeight();

        /* Create the unit grid and unit painter */
        unitGrid = new UnitGrid(n);
        unitPainter = new UnitPainter(unitGrid, mapPainter);

        /* Create the viewport */
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

        /* Listen for key presses */
        manager.registerKeyCode(KeyEvent.VK_LEFT);
        manager.registerKeyCode(KeyEvent.VK_RIGHT);
        manager.registerKeyCode(KeyEvent.VK_UP);
        manager.registerKeyCode(KeyEvent.VK_DOWN);
        manager.registerKeyCode(KeyEvent.VK_S);
        manager.registerKeyCode(KeyEvent.VK_W);
        manager.registerKeyCode(KeyEvent.VK_D);
        manager.registerKeyCode(KeyEvent.VK_1);
        manager.registerKeyCode(KeyEvent.VK_2);
        manager.registerKeyCode(KeyEvent.VK_3);
        manager.registerKeyCode(KeyEvent.VK_4);
        manager.registerKeyCode(KeyEvent.VK_5);
        manager.registerKeyCode(KeyEvent.VK_6);
        manager.registerKeyCode(KeyEvent.VK_7);
        manager.registerKeyCode(KeyEvent.VK_8);
        manager.registerKeyCode(KeyEvent.VK_9);
        manager.registerKeyCode(KeyEvent.VK_0);
        manager.registerKeyCode(KeyEvent.VK_BACK_QUOTE);
        manager.registerKeyCode(KeyEvent.VK_CONTROL);
    }

    /**
     * Update the game by one timestep. Called once per frame.
     */
    public void update(boolean focused){
        /* Respond to input if the game has focus (as opposed to the console) */
        if(focused){
            /* Grouping */
            int[] digits = {
                KeyEvent.VK_0, KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, 
                KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9
            };

            for(int x =0; x < 10; x++) {
                if (manager.getKeyCodeFlag(digits[x])) {
                    if (manager.getKeyCodeFlag(KeyEvent.VK_CONTROL)) {
                        SelectionStorage.store(Selection.current(), x);
                    }

                    /* Should check here if there are any other keys that have been depressed that would modify the number being pressed */
                    else if(!Selection.current().equals(SelectionStorage.retrieve(x))) {
                        if (SelectionStorage.retrieve(x) == null)
                            Selection.replaceCurrent(new Selection());
                        else
                            Selection.replaceCurrent(SelectionStorage.retrieve(x));
                        
                    }
                    manager.clearKeyCodeFlag(digits[x]);
                }
            }

            if(manager.getKeyCodeFlag(KeyEvent.VK_BACK_QUOTE) && !SelectionStorage.retrieve(10).isEmpty() && !Selection.current().equals(SelectionStorage.retrieve(10)) ) {
            	Selection s = SelectionStorage.retrieve(10);
                Selection.replaceCurrent(s);
                manager.clearKeyCodeFlag(KeyEvent.VK_BACK_QUOTE);
            }

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
                    BufferedImage art = ResourceManager.loadImage("resource/unit/spaceship/Art.png", 200, 200);
                    Sprite[] sprites = new Sprite[8];
                    for(Direction d : Direction.values()){
                        BufferedImage img = ResourceManager.loadImage("resource/unit/spaceship/Ship" + d.name() + ".png");
                        sprites[d.ordinal()]  = new Sprite(img, 0.3, 87, 25);
                    }

                    SimpleUnit spaceship = new SimpleUnit(getPlayer(), sprites, art, uSpeed, 0, 0, Direction.East, true);
                    tempUnit = spaceship;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(manager.getLeftMouseDown() && !mousePreviouslyPressed){
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
            if(!manager.getLeftMouseDown() && mousePreviouslyPressed && bottomRightSelection == null){
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
                            Selection newCurrent = Selection.current().clone();
                            if(Selection.current().contains(unit)){
                                newCurrent.remove(unit);
                            } else {
                                newCurrent.add(unit);
                            }
                            Selection.replaceCurrent(newCurrent);
                        }
                        else {
                            Selection newCurrent = Selection.current().clone();
                            newCurrent.clear();
                            newCurrent.add(unit);
                            Selection.replaceCurrent(newCurrent);
                        }
                    } else {
                        Selection.replaceCurrent(new Selection());
                    }
                }

            }

            if(manager.getMouseDragged() && topLeftSelection != null){
                Point point = manager.getMouseLocation();
                bottomRightSelection = point;

                SimpleUnit[] selectedUnits = unitPainter.getUnitsInRect(topLeftSelection, bottomRightSelection, viewport);

                if(!manager.getKeyCodeFlag(KeyEvent.VK_CONTROL)){
                    Selection.replaceCurrent(new Selection());
                }
                Selection newCurrent = Selection.current().clone();
                for(SimpleUnit unit : selectedUnits)
                    newCurrent.add(unit);
                Selection.replaceCurrent(newCurrent);

            } else if(!manager.getLeftMouseDown()){
                topLeftSelection = null;
                bottomRightSelection = null;
            }


            mousePreviouslyPressed = manager.getLeftMouseDown();

            /* Clicking (to set unit destination) */
            if(manager.getRightMouseClicked()){
                Point point = manager.getMouseLocation();
                Point unitTile = unitPainter.unitTileAtPoint(point, viewport);
                for(SimpleUnit unit : Selection.current().getList()){
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
            /* Mouse scrolling. Disabled for windowed mode. */
            if( Main.FULLSCREEN && manager.getMouseLocation().x > viewport.getWidth() - 30)
                viewport.translate(increment,0);
            if( Main.FULLSCREEN && manager.getMouseLocation().x < 30)
                viewport.translate(-increment,0);
            if( Main.FULLSCREEN && manager.getMouseLocation().y < 30)
                viewport.translate(0, -increment);
            if( Main.FULLSCREEN && manager.getMouseLocation().y > viewport.getHeight() - 30)
                viewport.translate(0, increment);
            

        }

        /* Update all units */
        unitPainter.update();
    }

    /**
     * Draw the game onto the screen.
     * @param graphics Graphics object on which to draw the game.
     */
    public void paint(Graphics2D graphics){
        /* Clear screen */
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, 2000, 2000);

        /* Move over to the viewport location */
        AffineTransform transform = graphics.getTransform();
        graphics.translate(-viewport.getX(), -viewport.getY());

        /* Paint the map using the map painter */
        mapPainter.paintMap(graphics, viewport);

        /* Paint the destination of all current selected units, if they share one */
        if(Selection.current().getList().size() != 0){
            /* Check if units share a destination */
            boolean shareDestination = true;
            Point destination = null;
            for(SimpleUnit unit : Selection.current().getList()){
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

        graphics.setTransform(transform);
    }

    /**
     * Draw the selection rectangle.
     * @param graphics Graphics2D object on which to draw
     */
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
            graphics.translate(-viewport.getX(), -viewport.getY());
        }
    }

    /**
     * Draw temporary units. Temporary units include things such as buildings which
     * are not yet placed on the map, but are being placed.
     * @param graphics Graphics2D object to draw with
     * @param viewport viewport in which the unit is being placed.
     */
    private void drawTemporaryUnits(Graphics2D graphics, Viewport viewport){
        unitPainter.paintTemporaryUnit(graphics, viewport, tempUnit, tempUnitX, tempUnitY);
    }

    /**
     * Get the map.
     * @return the current map
     */
    public Map getCurrentMap(){
        return map;
    }

    /**
     * Get the player
     * @return your player
     */
    public Player getPlayer(){
        return player;
    }

    /**
     * Get the unit grid.
     * @return current unit grid
     */
    public UnitGrid getUnitGrid(){
        return unitGrid;
    }

    /**
     * Get the viewport used to display the map.
     * @return viewport used to draw map
     */
    public Viewport getViewport(){
        return viewport;
    }

}
