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
import com.scriptrts.core.ClickAction;
import com.scriptrts.core.Main;
import com.scriptrts.core.PlaceAction;
import com.scriptrts.core.ui.Action;
import com.scriptrts.core.ui.HotkeyManager;
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
	 * Viewport used to display the map on the screen.
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

	public ClickAction getClickAction() {
		return clickAction;
	}

	public void setClickAction(ClickAction clickAction) {
		this.clickAction = clickAction;
	}

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

		HotkeyManager.registerHotkey(new Action("Create Headquarters"){
			public void execute() {
				try {
					Sprite[] sprites = ResourceManager.loadSpriteSet("headquarters.sprite", null);
					GameObject build = new GameObject(getPlayer(), sprites, ResourceManager.loadImage("resource/building/headquarters-frontal.png", 200, 200), 0, 0, 0, Direction.North, UnitShape.SHAPE_7x7, UnitClass.Building);
					Main.getGame().onClick(new PlaceAction(build));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, KeyEvent.VK_B);

		HotkeyManager.registerHotkey(new Action("Create Moving Unit"){
			public void execute() {
				int uSpeed = 9;
				try {
					/* Retrieve spaceship sprites */
					BufferedImage art = ResourceManager.loadImage("resource/unit/spaceship/Art.png", 200, 200);
					Sprite[] sprites = ResourceManager.loadSpriteSet("spaceship.sprite", getPlayer());
					GameObject spaceship = new GameObject(getPlayer(), sprites, art, uSpeed, 0, 0, Direction.East, UnitShape.SHAPE_1x1, UnitClass.Standard);
					Main.getGame().onClick(new PlaceAction(spaceship));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, KeyEvent.VK_M);

		HotkeyManager.registerHotkey(new Action("Create Drone"){
			public void execute() {
				int uSpeed = 9;
				try {
					/* Retrieve drone sprites */
					BufferedImage art = ResourceManager.loadImage("resource/unit/spaceship/Art.png", 200, 200);
					Sprite[] sprites = ResourceManager.loadSpriteSet("drone.sprite", getPlayer());
					GameObject drone = new GameObject(getPlayer(), sprites, art, uSpeed, 0, 0, Direction.East, UnitShape.SHAPE_1x1, UnitClass.Standard);
					Main.getGame().onClick(new PlaceAction(drone));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, KeyEvent.VK_W);

		HotkeyManager.registerHotkey(new Action("Create Cruiser"){
			public void execute(){
				int uSpeed = 5;
				try {
					/* Retrieve cruiser sprites */
					BufferedImage art = ResourceManager.loadImage("resource/unit/spaceship/Art.png", 200, 200);
					Sprite[] sprites = ResourceManager.loadSpriteSet("cruiser.sprite", getPlayer());
					GameObject cruiser = new GameObject(getPlayer(), sprites, art, uSpeed, 0, 0, Direction.East, UnitShape.SHAPE_5x5, UnitClass.Standard);
					Main.getGame().onClick(new PlaceAction(cruiser));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, KeyEvent.VK_E);

		HotkeyManager.registerHotkey(new Action("Create Stationary Unit"){
			public void execute() {
				int uSpeed = 0;
				try {
					/* Retrieve spaceship sprites */
					BufferedImage art = ResourceManager.loadImage("resource/unit/spaceship/Art.png", 200, 200);
					Sprite[] sprites = ResourceManager.loadSpriteSet("spaceship.sprite", getPlayer());
					GameObject spaceship = new GameObject(getPlayer(), sprites, art, uSpeed, 0, 0, Direction.East, UnitShape.SHAPE_1x1, UnitClass.Standard);
					Main.getGame().onClick(new PlaceAction(spaceship));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, KeyEvent.VK_D);

		HotkeyManager.registerHotkey(new Action("Create Volcano"){
			public void execute() {
				try {
					Sprite[] sprites = ResourceManager.loadSpriteSet("volcano.sprite", null);
					GameObject volcano = new GameObject(getPlayer(), sprites, null, 0, 0, 0, Direction.North, UnitShape.SHAPE_VOLCANO, UnitClass.Terrain);
					Main.getGame().onClick(new PlaceAction(volcano));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, KeyEvent.VK_V);
	}

	/**
	 * Handle game input. Called once per frame.
	 */
	public void handleInput(boolean focused, boolean mouseOverMap){
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
					else {
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

			HotkeyManager.update();


			if(mouseOverMap){
				if(manager.getLeftMouseDown() && !mousePreviouslyPressed){
					/* Get mouse location */
					Point point = manager.getMouseLocation();
					topLeftSelection = point;
					movedX = 0;
					movedY = 0;
				}

				/* Mouse released (from previously pressed position) without dragging or pressing shift */
				if(manager.getLeftMouseClicked() && !mousePreviouslyPressed){
					/* Get mouse location */
					Point point = manager.getMouseLocation();

					/* Click actions */
					if(clickAction != null){
						if(clickAction.click(point.x, point.y))
							clickAction = null;
					}

					else{
						/* Select or deselect a unit that was clicked on */
						GameObject unit = unitPainter.getUnitAtPoint(point, viewport);
						if(unit != null) {
							/* Whether the current selection is a building */
							boolean currentIsBuilding = Selection.current().isEmpty() || 
									Selection.current().getList().get(0).getUnit().isBuilding();
							Selection newCurrent;
							/* If already selected and pressing control, deselect */
							if(manager.getKeyCodeFlag(KeyEvent.VK_CONTROL)){
								newCurrent = Selection.current().clone();
								/* If the current selection contains the clicked unit, remove it and stop */
								if(Selection.current().contains(unit)){
									newCurrent.remove(unit);
									Selection.replaceCurrent(newCurrent);
								}
								else{
									/* If the current selection contains non-building units, add any non-building unit */
									if(!currentIsBuilding && !unit.getUnit().isBuilding()){
										newCurrent.add(unit);
										Selection.replaceCurrent(newCurrent);
									}
									/* If the current selection is a building, replace the current selection with the new selection */
									else if(currentIsBuilding){
										newCurrent.clear();
										newCurrent.add(unit);
										Selection.replaceCurrent(newCurrent);
									}
								}
							}
							else {
								newCurrent = Selection.current().clone();
								newCurrent.clear();
								newCurrent.add(unit);
								Selection.replaceCurrent(newCurrent);
							}
							Main.getOverlay().getButtonArea().updateButtons(newCurrent);
						} 

						/* If no unit was clicked on, deselect everything */
						else {
							Selection.replaceCurrent(new Selection());
							Main.getOverlay().getButtonArea().updateButtons(new Selection());
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
					Selection newCurrent = Selection.current();
					/* Check if the new selection contains stuff */
					if(selectedUnits.length > 0){
						/* Add all the newly selected units to a new selection */
						newCurrent = new Selection();
						for(GameObject unit : selectedUnits)
							newCurrent.add(unit);

								/* Whether the current selection is a building */
								boolean currentIsBuilding = Selection.current().isEmpty() || 
										Selection.current().getList().get(0).getUnit().isBuilding();

								/* Whether the new selection is a building */
								boolean newIsBuilding = selectedUnits.length > 0 && selectedUnits[0].getUnit().isBuilding();

								/* Combine the two current and new selections if both contain units */
								if(!currentIsBuilding){
									if(!newIsBuilding)
										newCurrent = Selection.combine(Selection.current(), newCurrent);
									else
										newCurrent = Selection.current();
								}
								Selection.replaceCurrent(newCurrent);
					}
					Main.getOverlay().getButtonArea().updateButtons(newCurrent);
				} else if(!manager.getLeftMouseDown()){
					topLeftSelection = null;
					bottomRightSelection = null;
				}

				/* Clicking (to set unit destination) */
				if(manager.getRightMouseClicked() || (manager.getRightMouseDown()) && !mousePreviouslyPressed){
					Point point = manager.getMouseLocation();
					Point unitTile = unitPainter.unitTileAtPoint(point, viewport);  
					/* If there is no unit at destination, move there */
					if(getGameGrid().getUnit(unitTile.x, unitTile.y) == null){
						for(GameObject unit : Selection.current().getList()){
							if(!unit.getUnit().isStandard())
								continue;
							if(manager.getKeyCodeFlag(KeyEvent.VK_SHIFT))
								unit.getUnit().getOrderHandler().queueOrder(new MoveOrder(unitTile));
							else
								unit.getUnit().getOrderHandler().order(new MoveOrder(unitTile));
						}
					} else {
						GameObject unitAtPoint = grid.getUnit(unitTile.x, unitTile.y);
						/* If the unit at the click location is a standard unit, issue a follow order */
						if(unitAtPoint.getUnit().isStandard()){
							for(GameObject unit : Selection.current().getList()){
								if(!unit.getUnit().isStandard())
									continue;
								if(manager.getKeyCodeFlag(KeyEvent.VK_SHIFT))
									unit.getUnit().getOrderHandler().queueOrder(new FollowOrder(unitAtPoint));
								else
									unit.getUnit().getOrderHandler().order(new FollowOrder(unitAtPoint));
							}
						}
						/* Otherwise issue a move order */
						else{
							for(GameObject unit : Selection.current().getList()){
								if(!unit.getUnit().isStandard())
									continue;
								if(manager.getKeyCodeFlag(KeyEvent.VK_SHIFT))
									unit.getUnit().getOrderHandler().queueOrder(new MoveOrder(unitTile));
								else
									unit.getUnit().getOrderHandler().order(new MoveOrder(unitTile));
							}
						}
					}

				}
				
				/* Prevent multiple actions from triggering */
				mousePreviouslyPressed = manager.getMouseDown();

			}

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
					for(GameObject unit : Selection.current().getList())
						unit.getUnit().getOrderHandler().order(new StopOrder());
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
	public void setCurrentMap(GameMap m){
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
			} else if(shareDestination){
				unitPainter.paintDestination(graphics, destination.x, destination.y);}
		}

		/* On top of the map, paint all the units and buildings */
		unitPainter.paintUnits(graphics, viewport);

		/* Draw fake cursors */
		if(clickAction != null && clickAction.hasCursor()){
			clickAction.paintCursor(graphics, viewport, manager.getMouseLocation().x, manager.getMouseLocation().y);
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

	/**
	 * Set the click action. 
	 * @param clickAct click action to be executed on next click
	 */
	public void onClick(ClickAction act){
		clickAction = act;
	}

	public InputManager getInputManager(){
		return manager;
	}

}
