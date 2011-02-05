package com.scriptrts.core;

import java.awt.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.Point;

import com.scriptrts.game.*;
import com.scriptrts.util.ResourceManager;

public class UnitPainter {

	/**
	 * The grid of units to draw
	 */
	private UnitGrid grid;

	/**
	 * The map painter on top of which units are being drawn
	 */
	private MapPainter mapPainter;

    /**
     * Enable debug drawing
     */
    public static boolean DEBUG = false;

	/**
	 * Create a new unit painter which paints the given units on the map
	 */
	public UnitPainter(UnitGrid g, MapPainter m){
		super();

		mapPainter = m;
		grid = g;
		
		try {
			/* Retrieve spaceship sprites */
			Sprite[] sprites = new Sprite[8];
            for(Direction d : Direction.values()){
                BufferedImage img = ResourceManager.loadImage("resource/unit/spaceship/Ship" + d.name() + ".png");
                sprites[d.ordinal()]  = new Sprite(img, 0.3, 87, 25);
            }
			/* Initialize the rider at the middle of the terrain tile (5,5), facing E.
			 *(Direction, at the moment, doesn't change. */
			SimpleUnit spaceship = new SimpleUnit(sprites, 3, 210, 186, Direction.East);


			/* Put the unit tile in the UnitGrid (to be associated with terrain tiles)*/
			grid.setUnit(spaceship, 210, 186);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * Change the zoom level by resizing all units
     * @param scale to apply to all sprites and images
     */
    public void zoom(double scale){
        /* Loop over all unit positions, scale where there are units */
        int n = mapPainter.getMap().getN() * UnitGrid.SPACES_PER_TILE;
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                if(grid.getUnit(i, j) != null)
                    for(Sprite s : grid.getUnit(i, j).getSprites())
                        s.scale(scale);
            }
        }
    }

    /**
     * Updates unit positions and animations
     */
    public void update(){
        /* Loop over all unit positions, update where there are units */
        int n = mapPainter.getMap().getN() * UnitGrid.SPACES_PER_TILE;
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                if(grid.getUnit(i, j) != null)
                    updateUnit(i, j);
            }
        }
    }

    /**
     * Update the unit at the given location 
     * @param i x coordinate in unit grid
     * @param j y coordinate in unit grid
     */
    private void updateUnit(int i, int j){
        /* Get fps */
        int fps = Main.getFPS();
        
        SimpleUnit unit = grid.getUnit(i, j);

        /* Unit speed is in subtiles per second */
        int uSpeed = unit.getSpeed();
        double subtilesMovedPerFrame = (double)(uSpeed) /* subtiles per second */ / fps /* times seconds */;

        int tilesMoved = unit.incrementAnimationCounter(subtilesMovedPerFrame);

        /* Move it however many tiles it wants to be moved */
        while(tilesMoved > 0){
            boolean moveSucceeded = grid.moveUnitOneTile(unit);
            if(moveSucceeded)
                tilesMoved--;
            else {
                unit.incrementAnimationCounter(tilesMoved - subtilesMovedPerFrame);
                tilesMoved = 0;
            }

        }
    }

	/**
	 * Paints all visible units onto the screen
	 */
    private int[] mapBoundsArray = new int[4];
	public void paintUnits(Graphics2D graphics, Viewport viewport){
		/* Calculate the viewport edges (in map tiles) */
		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();

        /* Get tiles visible */
        mapPainter.getViewportTileBounds(mapBoundsArray, viewport);
        int west    = mapBoundsArray[0];
        int east     = mapBoundsArray[1];
        int south   = mapBoundsArray[2];
        int north  = mapBoundsArray[3];

		for(int i = west; i < east; i++){
			for(int j = south; j < north; j++){
                paintUnitsOnMapTile(graphics, i, j);
			}
		}

	}

	private void paintUnitsOnMapTile(Graphics2D graphics, int i, int j){
		/* Calculate the pixel location of the tile on which we're drawing */
		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();

        /* Back corner pixel locations of tile */
        int x = (i+j+1)*tileX/2;
        int y = tileY * mapPainter.getMap().getN() / 2 + (i - j - 1) * tileY / 2;

		for(int a = 0; a < UnitGrid.SPACES_PER_TILE; a++){
            for(int b = 0; b < UnitGrid.SPACES_PER_TILE; b++){
                SimpleUnit unit = grid.getUnit(i * 3 + a, j * 3 + b);
                if(unit != null)
                    paintUnit(graphics, unit, x - tileX/2, y);
            }
		}

        if(UnitPainter.DEBUG){
            graphics.setColor(Color.green);
            for(int a = 0; a < UnitGrid.SPACES_PER_TILE; a++)
                for(int b = 0; b < UnitGrid.SPACES_PER_TILE; b++) {
                    Point backCorner = getUnitTileBackLocation(a, b);
                    backCorner.translate(x - tileX/2, y);
                    int[] xpts = {
                        backCorner.x, backCorner.x + tileX / 6, backCorner.x, backCorner.x - tileX / 6
                    };
                    int[] ypts = {
                        backCorner.y, backCorner.y + tileY / 6, backCorner.y + tileY / 3, backCorner.y + tileY / 6
                    };

                    graphics.drawPolygon(new Polygon(xpts, ypts, 4));
                }


        }
	}

    private Point getTileBackLocation(SimpleUnit unit){
        int tileBackX, tileBackY;

        /* Which piece of the terrain tile it's in */
        int a = unit.getX() % UnitGrid.SPACES_PER_TILE;
        int b = unit.getY() % UnitGrid.SPACES_PER_TILE;

        return getUnitTileBackLocation(a, b);
    }

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

	private void paintUnit(Graphics2D graphics, SimpleUnit unit, int tileLocX, int tileLocY){
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
		sprite.draw(graphics, xLoc, yLoc);

        /* Display selected units differently */
        if(unit.isSelected()){
            graphics.setColor(Color.RED);
            Rectangle bounds = sprite.getBounds(xLoc, yLoc);
            graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
	}

    public void paintTemporaryUnit(Graphics2D graphics, Viewport viewport, SimpleUnit unit, int xLoc, int yLoc){
        /* Draw the place it will snap to */
        Point pointOnScreen = new Point(xLoc, yLoc);
        Point unitTile = unitTileAtPoint(pointOnScreen, viewport);

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
        graphics.setColor(new Color(0, 255, 0, 120));
        graphics.fillPolygon(poly);
        graphics.setColor(Color.green);
        graphics.drawPolygon(poly);

        /* Draw the sprite on top */
        Sprite sprite = unit.getCurrentSprite();
		sprite.drawCentered(graphics, xLoc + viewport.getX(), yLoc + viewport.getY());
    }

    /**
     * Get the unit that is visible at the given point
     * @param point point in the viewport
     * @param viewport viewport that is being used to display the map
     */
    public SimpleUnit getUnitAtPoint(Point point, Viewport viewport){
        /* Treat a point as a very small rectangle of height and width 1 pixel */
        Point deltaPoint = new Point(point);
        point.translate(1, 1);

        /* Return the foremost unit */
        SimpleUnit[] unitsAtPoint = getUnitsInRect(point, deltaPoint, viewport);
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
    public SimpleUnit[] getUnitsInRect(Point topLeft, Point bottomRight, Viewport viewport){
        /* Store unit bounds and units on screen */
        ArrayList<Shape> unitPolys = new ArrayList<Shape>();
        ArrayList<SimpleUnit> unitsWithPolys = new ArrayList<SimpleUnit>();

        /* Calculate viewport boundaries */
        mapPainter.getViewportTileBounds(mapBoundsArray, viewport);
        int west    = mapBoundsArray[0];
        int east    = mapBoundsArray[1];
        int south   = mapBoundsArray[2];
        int north   = mapBoundsArray[3];

        /* Avoid modifying the input points, in case they will be used later by the caller */
        topLeft = new Point(topLeft);
        bottomRight = new Point(bottomRight);

        /* Translate the points to be on the map coordinates instead of in screen coordinates */
        topLeft.translate(viewport.getX(), viewport.getY());
        bottomRight.translate(viewport.getX(), viewport.getY());

        /* Rearrange coordinates so the top left point really is the top left, and bottom right really is bottom right */
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

        /* Clear polygons from previous click */
        unitPolys.clear();
        unitsWithPolys.clear();

        /* Loop through visible tiles. Loop backwards, because units in front take precedence. */
        for(int j = south; j <= north; j++)
            for(int b = 0; b < UnitGrid.SPACES_PER_TILE; b++)
                for(int i = west; i < east; i++) 
                    for(int a = 0; a < UnitGrid.SPACES_PER_TILE; a++)
                        addVisibleUnitPoly(unitPolys, unitsWithPolys, i * 3 + a, j * 3 + b);

        /* Bounds inside which we're looking for units */
        Rectangle rect = new Rectangle(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);

        /* Cound units inside bounds */
        int count = 0;
        for(int i = 0; i < unitPolys.size(); i++)
            if(rect.intersects(unitPolys.get(i).getBounds()))
                count++;

        /* Put units we've counted inside an array */
        SimpleUnit[] selected = new SimpleUnit[count];
        count = 0;
        for(int i = 0; i < unitPolys.size(); i++)
            if(rect.intersects(unitPolys.get(i).getBounds()))
                selected[count++] = unitsWithPolys.get(i);

        return selected;
    }

    /* Add units that are visible in a given unit tile to the list of visible units */
    private void addVisibleUnitPoly(ArrayList<Shape> unitShapes, ArrayList<SimpleUnit> addedUnits, int i, int j){
        SimpleUnit unit = grid.getUnit(i, j);
        if(unit == null) return;

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
     * @param point point to examing
     * @param viewport viewport through which map is being displayed
     */
    public Point unitTileAtPoint(Point point, Viewport viewport){
        Point mapCoords = mapPainter.getTileAtPoint(point, viewport);
        int iMap = mapCoords.x;
        int jMap = mapCoords.y;

        /* Avoid modifying original point object */
        point = new Point(point);

        /* Translate the point to be on the map coordinates instead of in screen coordinates */
        point.translate(viewport.getX(), viewport.getY());

        /* Find where this map tile is drawn */
        Point mapTileDrawnAt = mapPainter.getTileCoordinates(iMap, jMap);

        /* Now we can just draw a map tile and look at the coordinates of the point to figure out which part of the tile it's in */
        int tileX = mapPainter.getTileWidth();
        int tileY = mapPainter.getTileHeight();

        /* Try each unit tile in this map tile, return if the point is inside */
        for(int a = 0; a < UnitGrid.SPACES_PER_TILE; a++)
            for(int b = 0; b < UnitGrid.SPACES_PER_TILE; b++) {
                Point backCorner = getUnitTileBackLocation(a, b);
                backCorner.translate(mapTileDrawnAt.x, mapTileDrawnAt.y);
                /* The +- 5 elements make the tiles slightly bigger, to avoid floating point errors */
                int[] xpts = {
                   backCorner.x, backCorner.x + tileX / 6 + 5, backCorner.x, backCorner.x - tileX / 6 - 5
                };
                int[] ypts = {
                    backCorner.y - 5, backCorner.y + tileY / 6, backCorner.y + tileY / 3 + 5, backCorner.y + tileY / 6
                };


                if(new Polygon(xpts, ypts, 4).contains(point))
                    return new Point(a + iMap * UnitGrid.SPACES_PER_TILE, b + jMap * UnitGrid.SPACES_PER_TILE);
            }

        return null;
    }

}
