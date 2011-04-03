package com.scriptrts.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import com.scriptrts.control.FollowOrder;
import com.scriptrts.control.MoveOrder;
import com.scriptrts.control.Order;
import com.scriptrts.control.Selection;
import com.scriptrts.control.SelectionStorage;
import com.scriptrts.control.StopOrder;
import com.scriptrts.core.Main;
import com.scriptrts.core.ClickAction;
import com.scriptrts.core.PlaceAction;
import com.scriptrts.core.ui.InputManager;
import com.scriptrts.core.ui.MapPainter;
import com.scriptrts.core.ui.UnitPainter;
import com.scriptrts.core.ui.Viewport;
import com.scriptrts.util.ResourceManager;

/**
 * Game class which stores all information about a currently running game and manages
 * updating and drawing of the map and game features.
 */
public class Game extends HeadlessGame {

    /**
     * Viewport used to display the map on the scren.
     */ 
    private Viewport viewport;

    /**
     * Input manager used to deal with inputs throughout the application.
     */
    private InputManager manager = InputManager.getInputManager();

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
     * The unit currently being placed on the map, if a unit is being placed. It is drawn over the map,
     * and, unlike other units on the map, is not necessarily placed on the grid.
     */
    private GameObject tempUnit = null;

    /**
     * Location (in screen coordinates) at which the unit being placed is being placed.
     */
    private int tempUnitX, tempUnitY;

    /**
     * Whether the mouse was pressed on last update.
     */
    private boolean mousePreviouslyPressed = false;

    /**
     * Default scrolling increment
     */
    private static final int SCROLLING_DISTANCE = 30;

    /**
     * How many frames the map has been scrolling for
     */
    private int framesScrolled = 0;

    /**
     * Current increment in map scrolling
     */
    private int increment = SCROLLING_DISTANCE;

    private int movedX;
    private int movedY;

    /**
     * Action to take upon next map click
     */
    private ClickAction clickAction = null;

    /**
     * Create the game.
     * @param n size of the map (length along one edge)
     */
    public Game(int n, int width, int height) {
        super(n);

        /* Initialize viewport */
        viewport = new Viewport();
        viewport.setDim(width,  height);
    }

    /**
     * Initialize this game.
     */
    public void init(){
        super.init();

        /* Create the player */
        player = new Player("Player-One", Color.MAGENTA);
        addPlayer(player);

        /* Create painters */
        int tileX = 128;
        int tileY = 64;
        mapPainter = new MapPainter(map, tileX, tileY);
        unitPainter = new UnitPainter(grid, mapPainter);

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
        manager.registerKeyCode(KeyEvent.VK_M);
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
        manager.registerKeyCode(KeyEvent.VK_SHIFT);
        manager.registerKeyCode(KeyEvent.VK_C);
        manager.registerKeyCode(KeyEvent.VK_V);
        manager.registerKeyCode(KeyEvent.VK_B);
    }

    /**
     * Handle game input. Called once per frame.
     */
    public void handleInput(boolean focused){
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

            if(manager.getKeyCodeFlag(KeyEvent.VK_D) || manager.getKeyCodeFlag(KeyEvent.VK_M)){
                int uSpeed;
                if(manager.getKeyCodeFlag(KeyEvent.VK_D))
                    uSpeed = 0;
                else
                    uSpeed = 9;

                manager.clearKeyCodeFlag(KeyEvent.VK_M);
                manager.clearKeyCodeFlag(KeyEvent.VK_D);

                try {
                    /* Retrieve spaceship sprites */
                    BufferedImage art = ResourceManager.loadImage("resource/unit/spaceship/Art.png", 200, 200);
                    Sprite[] sprites = ResourceManager.loadSpriteSet("spaceship.sprite", getPlayer());
                    GameObject spaceship = new GameObject(getPlayer(), sprites, art, uSpeed, 0, 0, Direction.East, true, UnitClass.Standard);
                    clickAction = new PlaceAction(spaceship);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            if(manager.getKeyCodeFlag(KeyEvent.VK_V)){
            	manager.clearKeyCodeFlag(KeyEvent.VK_V);
            	
                try {
                    Sprite[] sprites = ResourceManager.loadSpriteSet("volcano.sprite", null);
                    GameObject volcano = new GameObject(getPlayer(), sprites, null, 0, 0, 0, Direction.North, true, UnitClass.Terrain);
                    clickAction = new PlaceAction(volcano);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            if(manager.getKeyCodeFlag(KeyEvent.VK_B)){
            	manager.clearKeyCodeFlag(KeyEvent.VK_B);
                try {
                    Sprite[] sprites = ResourceManager.loadSpriteSet("smithy.sprite", null);
                    GameObject volcano = new GameObject(getPlayer(), sprites, null, 0, 0, 0, Direction.North, true, UnitClass.Building);
                    clickAction = new PlaceAction(volcano);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(manager.getLeftMouseDown() && !mousePreviouslyPressed){
                /* Get mouse location */
                Point point = manager.getMouseLocation();
                topLeftSelection = point;
                movedX = 0;
                movedY = 0;

            }

            /* Mouse clicked (not dragged) */
            if(!manager.getLeftMouseDown() && mousePreviouslyPressed && bottomRightSelection == null && !manager.getKeyCodeFlag(KeyEvent.VK_SHIFT)){
                /* Get mouse location */
                Point point = manager.getMouseLocation();

                /* Click actions */
                if(clickAction != null){
                    clickAction.click(point.x, point.y);
                    clickAction = null;
                }

                else{
                    GameObject unit = unitPainter.getUnitAtPoint(point, viewport);
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

                GameObject[] selectedUnits = unitPainter.getUnitsInRect(topLeftSelection, bottomRightSelection, viewport);

                if(!manager.getKeyCodeFlag(KeyEvent.VK_CONTROL)){
                    Selection.replaceCurrent(new Selection());
                }
                Selection newCurrent = Selection.current().clone();
                for(GameObject unit : selectedUnits)
                    newCurrent.add(unit);
                Selection.replaceCurrent(newCurrent);

            } else if(!manager.getLeftMouseDown()){
                topLeftSelection = null;
                bottomRightSelection = null;
            }


            mousePreviouslyPressed = manager.getLeftMouseDown();

            if(manager.getKeyCodeFlag(KeyEvent.VK_C)){
                if(!Selection.current().getList().isEmpty()){
                    int size = Selection.current().getList().size();
                    GameObject unit = Selection.current().getList().get(size - 1);
                    viewport.setLocation(unitPainter.getUnitCoords(unit, viewport));
                }
            }
            
            if(manager.getKeyCodeFlag(KeyEvent.VK_S)){
                manager.clearKeyCodeFlag(KeyEvent.VK_S);
                if(!Selection.current().getList().isEmpty()){
                    // I don't think stop should be queueable, if you disagree, uncomment below.
                    /*if(manager.getKeyCodeFlag(KeyEvent.VK_SHIFT))
                        for(GameObject unit : Selection.current().getList())
                            unit.getOrderHandler().queueOrder(new StopOrder(unit));
                    else*/
                        for(GameObject unit : Selection.current().getList())
                            unit.getUnit().getOrderHandler().order(new StopOrder(unit));
                }
            }

            /* Clicking (to set unit destination) */
            if(manager.getRightMouseClicked()){
                Point point = manager.getMouseLocation();
                Point unitTile = unitPainter.unitTileAtPoint(point, viewport);    
                /* If there is no unit at destination, move there */
                //TODO: make it do something different if there is a unit there
                if(unitPainter.getGrid().getUnit(unitTile.x, unitTile.y) == null){
                    for(GameObject unit : Selection.current().getList()){
                        if(manager.getKeyCodeFlag(KeyEvent.VK_SHIFT))
                            unit.getUnit().getOrderHandler().queueOrder(new MoveOrder(unitTile, unit));
                        else
                            unit.getUnit().getOrderHandler().order(new MoveOrder(unitTile, unit));
                    }
                } else {
                    for(GameObject unit : Selection.current().getList()){
                        if(manager.getKeyCodeFlag(KeyEvent.VK_SHIFT))
                            unit.getUnit().getOrderHandler().queueOrder(new FollowOrder(unit,
                                        unitPainter.getGrid().getUnit(unitTile.x, unitTile.y)));
                        else
                            unit.getUnit().getOrderHandler().order(new FollowOrder(unit, 
                                        unitPainter.getGrid().getUnit(unitTile.x, unitTile.y)));
                    }
                }

            }

            /* Scrolling */
            boolean scrolling = false;

            int viewportPrevX = viewport.getX();
            int viewportPrevY = viewport.getY();

            if (manager.getKeyCodeFlag(KeyEvent.VK_RIGHT)) {
                viewport.translate(increment, 0);
                scrolling = true;
            }
            if (manager.getKeyCodeFlag(KeyEvent.VK_LEFT)) {
                viewport.translate(-increment, 0);
                scrolling = true;
            }
            if (manager.getKeyCodeFlag(KeyEvent.VK_UP)) {
                viewport.translate(0, -increment);
                scrolling = true;
            }
            if (manager.getKeyCodeFlag(KeyEvent.VK_DOWN)) {
                viewport.translate(0, increment);
                scrolling = true;
            }

            /* Mouse scrolling. Disabled for windowed mode. */
            if (Main.FULLSCREEN && manager.getMouseLocation().x > viewport.getWidth() - 30) {
                viewport.translate(increment, 0);
                scrolling = true;
            }
            if (Main.FULLSCREEN && manager.getMouseLocation().x < 30) {
                viewport.translate(-increment, 0);
                scrolling = true;
            }
            if (Main.FULLSCREEN && manager.getMouseLocation().y < 3) {
                viewport.translate(0, -increment);
                scrolling = true;
            }
            if (Main.FULLSCREEN && manager.getMouseLocation().y > viewport.getHeight() - 30) {
                viewport.translate(0, increment);
                scrolling = true;
            }

            if(scrolling){
                framesScrolled++;
                if(topLeftSelection != null)
                    topLeftSelection.translate(viewportPrevX - viewport.getX(), viewportPrevY - viewport.getY());
            }

            if(framesScrolled > 2 * Main.getFPS())
                increment = SCROLLING_DISTANCE * 4;
            else if(framesScrolled >  Main.getFPS())
                increment = SCROLLING_DISTANCE * 2;

            if(!scrolling){
                framesScrolled = 0;
                increment = SCROLLING_DISTANCE;
            }

            /* Update path handler */
            //TODO probably not the right place for this, but it works
            pathHandler.update();

        }
    }

    /**
     * Set the map
     * @param m new map
     */
    public void setCurrentMap(Map m){
        this.map = m;
        mapPainter.update();
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


            /* Check if units share the same order queue */
            boolean shareQueue = true;
            LinkedList<Order> queue = null;
            for(GameObject unit : Selection.current().getList()){
                if(queue == null)
                    queue = (LinkedList<Order>) unit.getUnit().getOrderHandler().getOrders();
                else
                    if(!queue.equals((LinkedList<Order>)unit.getUnit().getOrderHandler().getOrders())){
                        shareQueue = false;
                        break;
                    }
            }

            boolean shareDestination = false;
            Point destination = null;
            if(queue == null){
                /* Check if units share a destination */
                shareDestination = true;
                for(GameObject unit : Selection.current().getList()){
                    if(destination == null)
                        destination = unit.getUnit().getDestination();
                    else
                        if(!destination.equals(unit.getUnit().getDestination())){
                            shareDestination = false;
                            break;
                        }
                }
            }

            if(shareQueue && queue != null){
                drawDestinationQueue(graphics, queue);
            } else if(shareDestination)
                unitPainter.paintDestination(graphics, destination.x, destination.y);
        }

        /* On top of the map, paint all the units and buildings */
        unitPainter.paintUnits(graphics, viewport);

        /* Draw fake cursors */
        if(clickAction != null && clickAction.hasCursor()){
            graphics.translate(manager.getMouseLocation().x, manager.getMouseLocation().y);
            clickAction.paintCursor(graphics, viewport);
            graphics.translate(-manager.getMouseLocation().x, -manager.getMouseLocation().y);
        }

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
     * Draw the destination queue.
     */
    private void drawDestinationQueue(Graphics2D graphics, LinkedList<Order> queue){
        unitPainter.paintDestinationQueue(graphics, queue);
    }

    /**
     * Get the player
     * @return your player
     */
    public Player getPlayer(){
        return player;
    }

    /**
     * Get the viewport used to display the map.
     * @return viewport used to draw map
     */
    public Viewport getViewport(){
        return viewport;
    }

    /**
     * Get the unit painter
     * @return unit painter used to draw units on the map
     */
    public UnitPainter getUnitPainter(){
        return unitPainter;
    }

}
