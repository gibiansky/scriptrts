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
import com.scriptrts.core.InputManager;
import com.scriptrts.core.Main;
import com.scriptrts.core.Map;
import com.scriptrts.core.Viewport;
import com.scriptrts.core.ui.AnimatedSprite;
import com.scriptrts.core.ui.MapPainter;
import com.scriptrts.core.ui.Sprite;
import com.scriptrts.core.ui.EntityPainter;
import com.scriptrts.game.Entity.EntityType;
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
    private EntityPainter unitPainter;

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
     * The MapObject currently being placed on the map, if a unit is being placed. It is drawn over the map,
     * and, unlike other units on the map, is not necessarily placed on the grid.
     */
    private MapObject tempObj = null;

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

    /*)et
     * How many frames the map has been scrolling for
     */
    int framesScrolled = 0;

    /**
     * Current increment in map scrolling
     */
    int increment = SCROLLING_DISTANCE;

    int movedX;
    int movedY;

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
        unitPainter = new EntityPainter(grid, mapPainter);

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
                placingUnit = !placingUnit;
                int uSpeed;
                if(manager.getKeyCodeFlag(KeyEvent.VK_D))
                    uSpeed = 0;
                else
                    uSpeed = 9;

                manager.clearKeyCodeFlag(KeyEvent.VK_M);
                manager.clearKeyCodeFlag(KeyEvent.VK_D);

                Point point = manager.getMouseLocation();
                Point unitTile = unitPainter.unitTileAtPoint(point, viewport);
                tempUnitX = point.x;
                tempUnitY = point.y;

                try {
                    /* Retrieve spaceship sprites */
                    BufferedImage art = ResourceManager.loadImage("resource/unit/spaceship/Art.png", 200, 200);
                    Sprite[] sprites = new Sprite[16];
                    for(Direction d : Direction.values()){
                        String unitDir = "resource/unit/spaceship/";
                        String unitFilename = "Ship" + d.name() + ".png";
                        BufferedImage img = ResourceManager.loadBandedImage(
                                unitDir + unitFilename, unitDir + "allegiance/" + unitFilename, getPlayer().getColor());
                        sprites[d.ordinal()]  = new Sprite(img, 0.3, 87, 25);
                    }

                    for(Direction d : Direction.values()){
                        String unitDir = "resource/unit/spaceship/";
                        String unitFilename = "Ship" + d.name() + ".png";
                        BufferedImage normalImg = ResourceManager.loadBandedImage(
                                unitDir + unitFilename, unitDir + "allegiance/" + unitFilename, getPlayer().getColor());
                        BufferedImage attackImg = ResourceManager.loadBandedImage(
                                unitDir + "attack/" + unitFilename, unitDir + "allegiance/" + unitFilename, getPlayer().getColor());
                        int bX = 87, bY = 25;
                        if(d == Direction.Northwest)
                            bY += 43;
                        if(d == Direction.Southwest)
                            bX += 30;
                        sprites[8 + d.ordinal()]  = new AnimatedSprite(
                                new BufferedImage[]{
                                    normalImg, attackImg
                                }, new int[]{
                                    10, 10
                                }, 0.3, new int[]{
                                    87, bX
                                }, new int[]{
                                    25, bY
                                });
                    }
                    Unit spaceship = new Unit(getPlayer(), sprites, art, uSpeed, 0, 0, Direction.East, true, pathHandler);
                    tempObj = spaceship.getMapObject();
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

            if(placingUnit && manager.getMouseMoved()){
                Point point = manager.getMouseLocation();
                tempUnitX = point.x;
                tempUnitY = point.y;
            }

            /* Mouse released, but was never dragged */
            if(!manager.getLeftMouseDown() && mousePreviouslyPressed && bottomRightSelection == null && !manager.getKeyCodeFlag(KeyEvent.VK_SHIFT)){
                /* Get mouse location */
                Point point = manager.getMouseLocation();

                /* Adding units to map */
                if(placingUnit){
                    Point unitTile = unitPainter.unitTileAtPoint(point, viewport);
                    tempObj.getEntity().setX(unitTile.x);
                    tempObj.getEntity().setY(unitTile.y);
                    grid.placeMapObject(tempObj, unitTile.x, unitTile.y);
                    unitManager.addUnit((Unit)tempObj.getEntity());

                    if(Main.getGameClient() != null)
                        Main.getGameClient().sendNewUnitNotification(tempObj);

                    placingUnit = false;
                }
                else{
                    MapObject unit = unitPainter.getUnitAtPoint(point, viewport);
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

                MapObject[] selectedUnits = unitPainter.getUnitsInRect(topLeftSelection, bottomRightSelection, viewport);

                if(!manager.getKeyCodeFlag(KeyEvent.VK_CONTROL)){
                    Selection.replaceCurrent(new Selection());
                }
                Selection newCurrent = Selection.current().clone();
                for(MapObject unit : selectedUnits)
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
                    Entity entity = Selection.current().getList().get(size - 1).getEntity();
                    viewport.setLocation(unitPainter.getEntityCoords(entity, viewport));
                }
            }
            
            if(manager.getKeyCodeFlag(KeyEvent.VK_S)){
                manager.clearKeyCodeFlag(KeyEvent.VK_S);
                if(!Selection.current().getList().isEmpty()){
                    // I don't think stop should be queueable, if you disagree, uncomment below.
                    /*if(manager.getKeyCodeFlag(KeyEvent.VK_SHIFT))
                        for(SimpleUnit unit : Selection.current().getList())
                            unit.getOrderHandler().queueOrder(new StopOrder(unit));
                    else*/
                        for(MapObject obj : Selection.current().getList())
                            if(obj.getEntity() instanceof Unit)
                            	((Unit) obj.getEntity()).getOrderHandler().order(new StopOrder((Unit) obj.getEntity()));
                }
            }

            /* Clicking (to set unit destination) */
            if(manager.getRightMouseClicked()){
                Point point = manager.getMouseLocation();
                Point unitTile = unitPainter.unitTileAtPoint(point, viewport);    
                /* If there is no unit at destination, move there */
                //TODO: make it do something different if there is a unit there
                if(unitPainter.getGrid().getUnit(unitTile.x, unitTile.y) == null){
                    for(MapObject obj : Selection.current().getList()){
                    	if(obj.getEntity() instanceof Unit){
                    		if(manager.getKeyCodeFlag(KeyEvent.VK_SHIFT))
                    			((Unit) obj.getEntity()).getOrderHandler().queueOrder(new MoveOrder(unitTile, (Unit) obj.getEntity()));
                    		else
                    			((Unit) obj.getEntity()).getOrderHandler().order(new MoveOrder(unitTile, (Unit) obj.getEntity()));
                    	}
                    }
                } else {
                    for(MapObject obj : Selection.current().getList()){
                    	if(obj.getEntity() instanceof Unit){
                    		if(manager.getKeyCodeFlag(KeyEvent.VK_SHIFT))
                    			((Unit) obj.getEntity()).getOrderHandler().queueOrder(new FollowOrder((Unit) obj.getEntity(),
                    					unitPainter.getGrid().getUnit(unitTile.x, unitTile.y)));
                    		else
                    			((Unit) obj.getEntity()).getOrderHandler().order(new FollowOrder((Unit) obj.getEntity(), 
                    					unitPainter.getGrid().getUnit(unitTile.x, unitTile.y)));
                        }
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
            if (Main.FULLSCREEN
                    && manager.getMouseLocation().x > viewport.getWidth() - 30) {
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
            if (Main.FULLSCREEN
                    && manager.getMouseLocation().y > viewport.getHeight() - 30) {
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
            for(MapObject obj : Selection.current().getList()){
            	if(obj.getEntity() instanceof Unit){
            		if(queue == null)
            			queue = (LinkedList<Order>) ((Unit) obj.getEntity()).getOrderHandler().getOrders();
            		else
            			if(!queue.equals((LinkedList<Order>)((Unit) obj.getEntity()).getOrderHandler().getOrders())){
            				shareQueue = false;
            				break;
            			}
            	}
            }

            boolean shareDestination = false;
            Point destination = null;
            if(queue == null){
                /* Check if units share a destination */
                shareDestination = true;
                for(MapObject obj : Selection.current().getList()){
                	if(obj.getEntity() instanceof Unit){
                		if(destination == null)
                			destination = ((Unit) obj.getEntity()).getDestination();
                		else
                			if(!destination.equals(((Unit) obj.getEntity()).getDestination())){
                				shareDestination = false;
                				break;
                			}
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

        /* Draw fake units and buildings on the board */
        if(placingUnit)
            drawTemporaryObjects(graphics, viewport);

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
     * Draw temporary objects. Temporary units include things such as buildings which
     * are not yet placed on the map, but are being placed.
     * @param graphics Graphics2D object to draw with
     * @param viewport viewport in which the object is being placed.
     */
    private void drawTemporaryObjects(Graphics2D graphics, Viewport viewport){
        unitPainter.paintTemporaryObject(graphics, viewport, tempObj, tempUnitX, tempUnitY);
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

}