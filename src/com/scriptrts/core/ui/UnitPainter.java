package com.scriptrts.core.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import com.scriptrts.control.MoveOrder;
import com.scriptrts.control.Order;
import com.scriptrts.control.Selection;
import com.scriptrts.core.Main;
import com.scriptrts.game.Direction;
import com.scriptrts.game.GameObject;
import com.scriptrts.game.MapGrid;
import com.scriptrts.game.Sprite;
import com.scriptrts.game.UnitClass;
import com.scriptrts.util.ResourceManager;

/**
 * Painter which draws units onto the screen on top of the map.
 */
public class UnitPainter {

	/**
	 * The grid of units to draw
	 */
	private MapGrid grid;

	/**
	 * The map painter on top of which units are being drawn
	 */
	private MapPainter mapPainter;

	/**
	 * Image used to denote where a unit is going.
	 */
	private Image destinationImage = null;

	/**
	 * Image used to denote where a unit is going in the future.
	 */
	private Image destinationQueuedImage = null;

	/**
	 * Enable debug drawing
	 */
	public static boolean DEBUG = false;

	/**
	 * Integer array used internally for storage, so as not to create a new one each time update() is called.
	 */
	private int[] mapBoundsArray = new int[4];

	/**
	 * Create a new unit painter which paints the given units on the map
	 */
	public UnitPainter(MapGrid g, MapPainter m){
		super();

		mapPainter = m;
		grid = g;
	}

	/**
	 * Get the coordinates of the back point on the unit.
	 * @param unit which unit to get the back point of
	 */
	private Point getTileBackLocation(GameObject unit){
		/* Which piece of the terrain tile it's in */
		int a = unit.getUnit().getX() % MapGrid.SPACES_PER_TILE;
		int b = unit.getUnit().getY() % MapGrid.SPACES_PER_TILE;

		return getUnitTileBackLocation(a, b);
	}

	/**
	 * Get all the back points of all tiles a unit occupies.
	 * @param unit the unit to get tiles from
	 */
	private Point[] getAllTileBackLocations(GameObject unit){
		int[] xs = unit.getAllX();
		int[] ys = unit.getAllY();
		Point[] pts = new Point[xs.length];
		for(int i = 0; i < xs.length; i++){
			int a = xs[i] % MapGrid.SPACES_PER_TILE;
			int b = ys[i] % MapGrid.SPACES_PER_TILE;
			pts[i] = getUnitTileBackLocation(a,b);
		}
		return pts;
	}

	/**
	 * Get the tile back location of the given unit tile (inside the map tile)
	 */
	private Point getUnitTileBackLocation(int a, int b){
		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();

		int tileBackX, tileBackY;
		/* Bottom row */
		if(b == 0){
			/* South west */
			if(a == 0){
				tileBackX = tileX / 6;
				tileBackY = tileY / 3;
			} 
			/*  South */
			else if(a == 1){
				tileBackX = tileX / 3;
				tileBackY = tileY / 2;
			}
			/* Southeast */
			else {
				tileBackX = tileX / 2;
				tileBackY = 2 * tileY / 3;
			}
		}

		/* Middle row */
		else if(b == 1){
			/* West */
			if(a == 0){
				tileBackX = tileX / 3;
				tileBackY = tileY / 6;
			} 
			/* Center */
			else if(a == 1){
				tileBackX = tileX / 2;
				tileBackY = tileY / 3;
			}
			/* East */
			else {
				tileBackX = 2 * tileX / 3;
				tileBackY = tileY / 2;
			}
		}

		/* Top row */
		else {
			/* Northwest */
			if(a == 0){
				tileBackX = tileX / 2;
				tileBackY = 0;
			} 
			/* North */
			else if(a == 1){
				tileBackX = 2 * tileX / 3;
				tileBackY = tileY / 6;
			}
			/* Northeast */
			else {
				tileBackX = 5*tileX / 6;
				tileBackY = tileY / 3;
			}
		}

		/* TODO: we don't need to create a new point... just have these be private global variables... */
		return new Point(tileBackX, tileBackY);
	}

	/**
	 * Paints all visible units onto the screen
	 * @param graphics graphics handle to the screen
	 * @param viewport viewport being used to view the map
	 */
	public void paintUnits(Graphics2D graphics, Viewport viewport){
		/* Get tiles visible */
		mapPainter.getViewportTileBounds(mapBoundsArray, viewport);
		int west    = mapBoundsArray[0];
		int east     = mapBoundsArray[1];
		int south   = mapBoundsArray[2];
		int north  = mapBoundsArray[3];

		for(int i = west; i < east; i++){
			for(int j = south; j < north; j++){
				paintUnitsOnMapTile(graphics, i, j);
				/* If we are currently placing a unit, draw occupied unit tiles */
				if(Main.getGame().getClickAction() != null)
					paintUnitSquares(graphics, i, j);
			}
		}

		if(UnitPainter.DEBUG)
			for(int i = west; i < east; i++)
				for(int j = south; j < north; j++)
					paintUnitSquares(graphics, i, j);
	}

	/**
	 * Paints the units which are on a given map tile.
	 * @param graphics graphics handle to the screen
	 * @param i x coordinate of the map tile
	 * @param j y coordinate of the map tile
	 */
	private void paintUnitsOnMapTile(Graphics2D graphics, int i, int j){
		/* Calculate the pixel location of the tile on which we're drawing */
		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();

		/* Back corner pixel locations of tile */
		int x = (i+j+1)*tileX/2;
		int y = tileY * mapPainter.getMap().getN() / 2 + (i - j - 1) * tileY / 2;

		GameObject unit;
		for(int a = 0; a < MapGrid.SPACES_PER_TILE; a++){
			for(int b = 0; b < MapGrid.SPACES_PER_TILE; b++){
				unit = grid.getUnit(i * 3 + a, j * 3 + b);
				if(unit != null && i * 3 + a == unit.getUnit().getX() && j * 3 + b == unit.getUnit().getY())
					paintUnit(graphics, unit, x - tileX/2, y);
			}
		}
	}

	/**
	 * Paint a single unit onto the map.
	 * @param graphics graphics handle to the screen
	 * @param unit which unit to paint onto the screen
	 * @param tileLocX graphical x coordinate of the map tile this unit is on
	 * @param tileLocY graphical y coordinate of the map tile this unit is on
	 */
	private void paintUnit(Graphics2D graphics, GameObject unit, int tileLocX, int tileLocY){
		/* How far the unit has moved from its current tile to its destination */
		double percentMovedFromTile = unit.getAnimationCounter();

		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();

		/* Find the back point of the tile it's currently placed in */
		Point backStartSubtile = getTileBackLocation(unit);

		/* Find the shift necessary to get from the back point of its current tile
		 * to the back point of the tile it's going to
		 */
		Point backShift = Direction.getShift(mapPainter, unit.getDirection());

		/* Calculate where it is based on where it started, where it's going, and how far it's gone */
		int tileBackX = (int)(backStartSubtile.getX()  + percentMovedFromTile * backShift.getX());
		int tileBackY = (int)(backStartSubtile.getY()  + percentMovedFromTile * backShift.getY());

		/* Make the back of the unit agree with the back of the tile */
		int xLoc = tileLocX + tileBackX;
		int yLoc = tileLocY + tileBackY;

		Sprite sprite = unit.getCurrentSprite();

		/* Display selected units differently */
		if(Selection.current().contains(unit)){
			graphics.setColor(Color.RED);
			Rectangle bounds = sprite.getBounds(xLoc, yLoc);
			graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

			int[] xs = unit.getAllX();
			int[] ys = unit.getAllY();
			Point[] pts = getAllTileBackLocations(unit);
			for(int k = 0; k < xs.length; k++){
				int i = xs[k] / 3;
				int j = ys[k] / 3;
				int x = (i+j+1)*tileX/2;
				int y = tileY * mapPainter.getMap().getN() / 2 + (i - j - 1) * tileY / 2;

				Point backCorner = pts[k];
				backCorner.translate(x - tileX/2, y);

				/* Polygon around the unit tile */
				int[] xpts = {
						backCorner.x, backCorner.x + tileX / 6 + 2, backCorner.x, backCorner.x - tileX / 6 - 2
				};
				int[] ypts = {
						backCorner.y - 2, backCorner.y + tileY / 6, backCorner.y + tileY / 3 + 2, backCorner.y + tileY / 6
				};
				Polygon poly = new Polygon(xpts, ypts, 4);

				/* Draw half transparent polygons where the unit will go */
				graphics.setColor(new Color(0, 255, 0, 120));
				graphics.fillPolygon(poly);
				graphics.setColor(Color.green);
				graphics.drawPolygon(poly);

				/* Draw the health bar */
				graphics.setColor(Color.GREEN);
				/* The width of the unit's current health */
				int greenWidth = sprite.getWidth() * unit.getUnit().getHealth() / unit.getUnit().hitpoints();
				graphics.fillRect(xLoc - sprite.getWidth() / 2, yLoc - sprite.getHeight() + 25, greenWidth, 5);
				/* Draw the missing portion of the unit's health */
				graphics.setColor(Color.RED);
				graphics.fillRect(xLoc - sprite.getWidth() / 2 + greenWidth, yLoc - sprite.getHeight() + 25, sprite.getWidth() - greenWidth, 5);
			}
		}

		sprite.draw(graphics, unit, xLoc, yLoc);
	}

	/**
	 * Paint a unit that is currently being placed on the map.
	 * @param graphics graphics handle to the screen
	 * @param viewport viewport being used to view the map
	 * @param unit which unit to draw
	 * @param xLoc x location of the mouse
	 * @param yLoc y location of the mouse
	 */
	public void paintTemporaryUnit(Graphics2D graphics, Viewport viewport, GameObject unit, int xLoc, int yLoc){
		if(unit == null)
			return;

		/* Draw the place it will snap to */
		Point pointOnScreen = new Point(xLoc, yLoc);
		Point unitTile;

		for(Point p : unit.getShape(unit.getFacingDirection())){
			unitTile = unitTileAtPoint(pointOnScreen, viewport);
			unitTile.translate(p.x, p.y);

			/* Back corner pixel locations of map tile */
			int iMap = unitTile.x / 3;
			int jMap = unitTile.y / 3;
			int tileX = mapPainter.getTileWidth();
			int tileY = mapPainter.getTileHeight();
			int x = (iMap+jMap+1)*tileX/2;
			int y = tileY * mapPainter.getMap().getN() / 2 + (iMap - jMap - 1) * tileY / 2;

			/* Indices inside the map tile */
			int a = unitTile.x % 3;
			int b = unitTile.y % 3;
			Point backCorner = getUnitTileBackLocation(a, b);
			backCorner.translate(x - tileX/2, y);

			/* Polygon around the unit tile */
			int[] xpts = {
					backCorner.x, backCorner.x + tileX / 6 + 2, backCorner.x, backCorner.x - tileX / 6 - 2
			};
			int[] ypts = {
					backCorner.y - 2, backCorner.y + tileY / 6, backCorner.y + tileY / 3 + 2, backCorner.y + tileY / 6
			};
			Polygon poly = new Polygon(xpts, ypts, 4);

			/* Draw half transparent polygons where the unit will go */
			if(grid.getUnit(unitTile.x, unitTile.y) == null){
				graphics.setColor(new Color(0, 255, 0, 120));
				graphics.fillPolygon(poly);
				graphics.setColor(Color.green);
				graphics.drawPolygon(poly);
			}
			else{
				graphics.setColor(new Color(255, 0, 0, 120));
				graphics.fillPolygon(poly);
				graphics.setColor(Color.red);
				graphics.drawPolygon(poly);
			}
		}

		/* Draw the sprite on top */
		Sprite sprite = unit.getCurrentSprite();
		Composite composite = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
		sprite.drawCentered(graphics, xLoc + viewport.getX(), yLoc + viewport.getY());
		graphics.setComposite(composite);
	}

	public void paintUnitSquares(Graphics2D graphics, int i, int j){
		/* Paint taken up squares as blue */

		/* Calculate the pixel location of the tile on which we're drawing */
		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();

		/* Back corner pixel locations of tile */
		int x = (i+j+1)*tileX/2;
		int y = tileY * mapPainter.getMap().getN() / 2 + (i - j - 1) * tileY / 2;

		for(int a = 0; a < MapGrid.SPACES_PER_TILE; a++)
			for(int b = 0; b < MapGrid.SPACES_PER_TILE; b++) {
				if(Main.getGame().getGameGrid().spaceTakenFor(i * 3 + a, j * 3 + b, null)){
					Point backCorner = getUnitTileBackLocation(a, b);
					backCorner.translate(x - tileX/2, y);
					int[] xpts = {
							backCorner.x, backCorner.x + tileX / 6, backCorner.x, backCorner.x - tileX / 6
					};
					int[] ypts = {
							backCorner.y, backCorner.y + tileY / 6, backCorner.y + tileY / 3, backCorner.y + tileY / 6
					};

					Polygon poly = new Polygon(xpts, ypts, 4);
					graphics.setColor(new Color(0, 0, 255, 120));
					graphics.fillPolygon(poly);
					graphics.setColor(Color.blue);
					graphics.drawPolygon(poly);
				}
			}
	}

	/**
	 * Get the unit that is visible at the given point
	 * @param point point in the viewport
	 * @param viewport viewport that is being used to display the map
	 */
	public GameObject getUnitAtPoint(Point point, Viewport viewport){
		/* Treat a point as a very small rectangle of height and width 30 pixels */
		point.translate(-15, -15);
		Point deltaPoint = new Point(point);
		deltaPoint.translate(30, 30);

		/* Return the foremost unit */
		GameObject[] unitsAtPoint = getUnitsInRect(point, deltaPoint, viewport);
		if(unitsAtPoint.length == 0) 
			return null;
		else 
			return unitsAtPoint[0];
	}

	/**
	 * Get the unit that is visible inside a given rectangle on the viewport
	 * @param topLeft top left bound point of the rectangle
	 * @param bottomRight bottom right bound point of the rectangle
	 * @param viewport viewport that is being used to display the map
	 */
	public GameObject[] getUnitsInRect(Point topLeft, Point bottomRight, Viewport viewport){
		/* Store unit bounds and units on screen */
		ArrayList<Shape> unitPolys = new ArrayList<Shape>();
		ArrayList<GameObject> unitsWithPolys = new ArrayList<GameObject>();

		/* Avoid modifying the input points, in case they will be used later by the caller */
		topLeft = new Point(topLeft);
		bottomRight = new Point(bottomRight);

		/* Keep track of the direction the rectangle was selected from */
		boolean topToBottom = true;
		boolean leftToRight = true;

		/* Rearrange coordinates so the top left point really is the top left, and bottom right really is bottom right */
		if(topLeft.x > bottomRight.x){
			int temp = topLeft.x;
			topLeft.x = bottomRight.x;
			bottomRight.x = temp;
			leftToRight = false;
		}
		if(topLeft.y > bottomRight.y){
			int temp = topLeft.y;
			topLeft.y = bottomRight.y;
			bottomRight.y = temp;
			topToBottom = false;
		}

		/* Find bounds of map in unit tiles */
		int minX = Math.min(topLeft.x, -30);
		int minY = Math.min(topLeft.y, -30);
		int maxX = Math.max(bottomRight.x, viewport.getX() + 30);
		int maxY = Math.max(bottomRight.y, viewport.getY() + 30);
		Point mapTL = new Point(minX, minY);
		Point mapBR = new Point(maxX, maxY);
		int topLeftTileX = unitTileAtPoint(mapTL, viewport).x;
		int topRightTileY = unitTileAtPoint(new Point(mapBR.x, mapTL.y), viewport).y;
		int bottomLeftTileY = unitTileAtPoint(new Point(mapTL.x, mapBR.y), viewport).y;
		int bottomRightTileX = unitTileAtPoint(mapBR, viewport).x;

		/* Translate the points to be on the map coordinates instead of in screen coordinates */
		topLeft.translate(viewport.getX(), viewport.getY());
		bottomRight.translate(viewport.getX(), viewport.getY());

		/* Clear polygons from previous click */
		unitPolys.clear();
		unitsWithPolys.clear();

		/* Loop through visible tiles in the direction they were selected.
		 * This is so the first building gets selected if there are multiple.
		 * This part is kind of ugly, even though it's basically 2 for loops. */
		if(topToBottom){
			/* Top left to bottom right */
			if(leftToRight){
				for(int i = topLeftTileX; i <= bottomRightTileX; i++)
					for(int j = bottomLeftTileY; j <= topRightTileY; j++)
						addVisibleUnitPoly(unitPolys, unitsWithPolys, i, j);
			}
			/* Top right to bottom left */
			else {
				for(int j = topRightTileY; j >= bottomLeftTileY; j--)
					for(int i = bottomRightTileX; i >= topLeftTileX; i--)
						addVisibleUnitPoly(unitPolys, unitsWithPolys, i, j);
			}			
		} else {
			/* Bottom left to top right */
			if(leftToRight){
				for(int j = bottomLeftTileY; j <= topRightTileY; j++)
					for(int i = topLeftTileX; i <= bottomRightTileX; i++)
						addVisibleUnitPoly(unitPolys, unitsWithPolys, i, j);
			}
			/* Bottom right to top left */
			else {
				for(int i = bottomRightTileX; i >= topLeftTileX; i--)
					for(int j = topRightTileY; j >= bottomLeftTileY; j--)
						addVisibleUnitPoly(unitPolys, unitsWithPolys, i, j);
			}
		}

		/* Bounds inside which we're looking for units */
		Rectangle rect = new Rectangle(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);

		/* Count units (including buildings) inside bounds */
		int count = 0;
		/* Count number of buildings inside bounds */
		int nBuildings = 0;
		for(int i = 0; i < unitPolys.size(); i++){
			if(rect.intersects(unitPolys.get(i).getBounds())){
				count++;
				if(unitsWithPolys.get(i).getUnit().isBuilding())
					nBuildings++;
			}
		}

		/* Put units we've counted inside an array */
		GameObject[] selected;

		/* If we have only selected buildings, return the first building selected */
		if(nBuildings == count && nBuildings > 0){
			selected = new GameObject[1];
			for(int i = 0; i < unitPolys.size(); i++){
				if(unitsWithPolys.get(i).getUnit().isBuilding() && rect.intersects(unitPolys.get(i).getBounds())){
					selected[0] = unitsWithPolys.get(i);
					break;
				}
			}
		}
		/* Otherwise return all non-building units selected */
		else {
			selected = new GameObject[count - nBuildings];
			count = 0;
			for(int i = 0; i < unitPolys.size(); i++)
				if(!unitsWithPolys.get(i).getUnit().isBuilding() && rect.intersects(unitPolys.get(i).getBounds()))
					selected[count++] = unitsWithPolys.get(i);
		}

		return selected;
	}

	/**
	 * Add units that are visible in a given unit tile to the list of visible units 
	 * @param unitShapes list of polygons which represent unit boundaries
	 * @param addedUnits list of units which were detected
	 * @param i x coordinate (unit tiles) of the unit to add
	 * @param j y coordinate (unit tiles) of the unit to to the polygon and unit selection
	 */
	private void addVisibleUnitPoly(ArrayList<Shape> unitShapes, ArrayList<GameObject> addedUnits, int i, int j){
		GameObject unit = grid.getUnit(i, j);
		if(unit == null) return;
		if(unit.getUnit().isTerrain()) return;
		/* Check that this is the tile the unit is centered on */
		if(unit.getUnit().getX() != i || unit.getUnit().getY() != j)
			return;

		/* Calculate the pixel location of the tile on which we're looking for units */
		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();

		/* Convert i and j to map tiles */
		i /= 3;
		j /= 3;

		/* Back corner pixel locations of tile */
		int x = (i+j+1)*tileX/2;
		int y = tileY * mapPainter.getMap().getN() / 2 + (i - j - 1) * tileY / 2;

		/* where the tile image is */
		int tileLocX = x - tileX / 2;
		int tileLocY = y;


		double percentMovedFromTile = unit.getAnimationCounter();

		/* Find the back point of the tile it's currently placed in */
		Point backStartSubtile = getTileBackLocation(unit);

		/* Find the shift necessary to get from the back point of its current tile
		 * to the back point of the tile it's going to
		 */
		Point backShift = Direction.getShift(mapPainter, unit.getDirection());

		/* Calculate where it is based on where it started, where it's going, and how far it's gone */
		int tileBackX = (int)(backStartSubtile.getX()  + percentMovedFromTile * backShift.getX());
		int tileBackY = (int)(backStartSubtile.getY()  + percentMovedFromTile * backShift.getY());

		/* The location of the tile image we're drawing on */
		int xLoc = tileLocX + tileBackX;
		int yLoc = tileLocY + tileBackY;

		Sprite sprite = unit.getCurrentSprite();
		Rectangle boundingBox = sprite.getBounds(xLoc, yLoc);

		unitShapes.add(boundingBox);
		addedUnits.add(unit);
	}

	/**
	 * Find which part of the map tile was clicked on, in unit tile coordinates
	 * @param point point to examine
	 * @param viewport viewport through which map is being displayed
	 */
	public Point unitTileAtPoint(Point point, Viewport viewport){
		double tileX = (double) mapPainter.getTileWidth() / (double) MapGrid.SPACES_PER_TILE;
		double tileY = (double) mapPainter.getTileHeight() / (double) MapGrid.SPACES_PER_TILE;
		int n = MapGrid.SPACES_PER_TILE * mapPainter.getMap().getN();
		int mapHeight = mapPainter.getTileHeight() * mapPainter.getMap().getN();

		/* Avoid modifying the input point, in case they will be used later by the caller */
		point = new Point(point);
		/* Point in map coordinates */
		point.translate(viewport.getX(), viewport.getY());

		/* Solve the linear transformation */
		double term1 = point.x / (tileX / 2);
		double term2 = (point.y - mapHeight / 2) / (tileY / 2);

		/* Coordinates of the unit tile clicked on */
		int gridX = (int) ((term1 + term2) / 2);
		int gridY = (int) ((term1 - term2) / 2);

		/* If tile is not on map, choose point on boundary */
		if(gridX < 0) gridX = 0;
		if(gridX > n) gridX = n;
		if(gridY < 0) gridY = 0;
		if(gridY > n) gridY = n;

		Point unitGridTile = new Point(gridX, gridY);
		return unitGridTile;
	}

	/**
	 * Get the map coordinates of the given unit
	 * @param unit the unit to get the coordinates of
	 * @param viewport viewport through which map is being displayed
	 * @return
	 */
	public Point getUnitCoords(GameObject unit, Viewport viewport){    	
		double tileX = (double) mapPainter.getTileWidth() / (double) MapGrid.SPACES_PER_TILE;
		double tileY = (double) mapPainter.getTileHeight() / (double) MapGrid.SPACES_PER_TILE;
		int mapHeight = mapPainter.getTileHeight() * mapPainter.getMap().getN();

		/* Coordinates of the unit tile the unit is on */
		int x = unit.getUnit().getX();
		int y = unit.getUnit().getY();

		/* Change into map coordinates */
		int mapX = (int) ((x + y) * tileX - viewport.getWidth()) / 2;
		int mapY = (int) ((x - y) * tileY + mapHeight - viewport.getHeight()) / 2;

		Point mapCoords = new Point(mapX, mapY);
		return mapCoords;
	}

	/**
	 * Paint the destination image at the given location 
	 * @param graphics graphics handle used to draw the image
	 * @param i x coordinate in unit tiles of the destination
	 * @param j y coordinate in unit tiles of the destination
	 */
	public void paintDestination(Graphics2D graphics, int i, int j){
		if(destinationImage == null)
			try {
				destinationImage = ResourceManager.loadImage("resource/Destination.png", 50, 50);
			} catch (IOException io) {
				io.printStackTrace();
			}

			int iMap = i / 3;
			int jMap = j / 3;
			int tileX = mapPainter.getTileWidth();
			int tileY = mapPainter.getTileHeight();
			int x = (iMap+jMap)*tileX/2;
			int y = tileY * mapPainter.getMap().getN() / 2 + (iMap - jMap - 1) * tileY / 2;

			/* Indices inside the map tile */
			int a = i % 3;
			int b = j % 3;
			Point backCorner = getUnitTileBackLocation(a, b);
			backCorner.translate(x, y);

			graphics.drawImage(destinationImage, 
					backCorner.x - destinationImage.getWidth(null)/2, backCorner.y - destinationImage.getHeight(null)/2, null);
	}

	/**
	 * Paints the destination queue
	 * @param graphics
	 * @param queue
	 */
	public void paintDestinationQueue(Graphics2D graphics, LinkedList<Order> queue) {
		if(destinationImage == null)
			try {
				destinationImage = ResourceManager.loadImage("resource/Destination.png", 50, 50);
			} catch (IOException io) {
				io.printStackTrace();
			}

			if(destinationQueuedImage == null)
				try {
					destinationQueuedImage = ResourceManager.loadImage("resource/Destination_queued.png", 50, 50);
				} catch (IOException io) {
					io.printStackTrace();
				}

				boolean first = true;
				int number = 0;
				if(queue.peek() != null)
					for(Order order : queue){
						if(!(order instanceof MoveOrder)) continue;

						int i = ((MoveOrder) order).getPoint().x;
						int j = ((MoveOrder) order).getPoint().y;

						int iMap = i / 3;
						int jMap = j / 3;
						int tileX = mapPainter.getTileWidth();
						int tileY = mapPainter.getTileHeight();
						int x = (iMap+jMap)*tileX/2;
						int y = tileY * mapPainter.getMap().getN() / 2 + (iMap - jMap - 1) * tileY / 2;

						/* Indices inside the map tile */
						int a = i % 3;
						int b = j % 3;
						Point backCorner = getUnitTileBackLocation(a, b);
						backCorner.translate(x, y);

						Image imageToUse = first ? destinationImage : destinationQueuedImage;

						graphics.drawImage(imageToUse, 
								backCorner.x - imageToUse.getWidth(null)/2, backCorner.y - imageToUse.getHeight(null)/2, null);

						number++;

						graphics.setColor(Color.white);
						/* If two digits, shift left a little */
						// TODO: this could be not hard-coded
						int rightShift = Math.log10(number) >= 1 ? 19 : 23;
						if(!first){
							graphics.drawString(number + "",backCorner.x - imageToUse.getWidth(null)/2 + rightShift,
									backCorner.y - imageToUse.getHeight(null)/2 + 42);
						}

						first = false;

					}
	}
}
