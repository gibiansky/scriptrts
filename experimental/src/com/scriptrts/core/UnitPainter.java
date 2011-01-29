package com.scriptrts.core;

import java.awt.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.Point;

import com.scriptrts.game.Direction;
import com.scriptrts.game.SimpleUnit;
import com.scriptrts.game.UnitGrid;
import com.scriptrts.game.UnitTile;
import com.scriptrts.util.ResourceManager;

public class UnitPainter {

	/** 
	 * How many of the smallest unit can fit along one side of each map tile
	 */
	public final static int SPACES_PER_TILE = 3;

	/**
	 * The grid of units to draw
	 */
	private UnitGrid grid;

	/**
	 * The map painter on top of which units are being drawn
	 */
	private MapPainter mapPainter;

	/**
	 * Create a new unit painter which paints the given units on the map
	 */
	public UnitPainter(UnitGrid g, MapPainter m){
		super();

		mapPainter = m;
		grid = g;
		
		try {
			UnitTile tile = new UnitTile();
			tile.units = new SimpleUnit[9];
			/* Retrieve rider sprites */
			BufferedImage[] sprites = {ResourceManager.loadImage("resource/unit/rider/FemaleRider.png"),
					ResourceManager.loadImage("resource/unit/rider/FemaleRiderMoving.png")};
			/* Initialize the rider at the middle of the terrain tile (5,5), facing E.
			 *(Direction, at the moment, doesn't change. */
			SimpleUnit rider = new SimpleUnit(sprites, 1, 5, 5, UnitLocation.Center, Direction.East);
			/* Place the rider in the unit tile */
			tile.units[UnitLocation.Center.ordinal()] = rider;
			/* Put the unit tile in the UnitGrid (to be associated with terrain tiles)*/
			grid.unitGrid[5][5] = tile;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    /**
     * Updates unit positions and animations
     */
    public void update(){
        int n = mapPainter.getMap().getN();
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                UnitTile tile = grid.unitGrid[i][j];
                if(tile != null){
                    updateUnitTile(i, j);
                }
            }
        }
    }

    private void updateUnitTile(int i, int j){
        UnitTile tile = grid.unitGrid[i][j];
        for(UnitLocation loc : UnitLocation.values()){
            SimpleUnit unit = tile.units[loc.ordinal()];
            if(unit != null && unit.unitLocation == loc){
                updateUnit(i, j, loc);
            }
        }
    }

    private void updateUnit(int i, int j, UnitLocation loc){
        int fps = 30;
        
        UnitTile tile = grid.unitGrid[i][j];
        SimpleUnit unit = tile.units[loc.ordinal()];

        /* Unit speed is in subtiles per second */
        int uSpeed = unit.getSpeed();
        double subtilesMovedPerFrame = (double)(uSpeed) /* subtiles per second */ / fps /* times seconds */;

        int subtilesMoved = unit.incrementAnimationCounter(subtilesMovedPerFrame);

        /* Move it however many subtiles it wants to be moved */
        while(subtilesMoved > 0){
            grid.moveUnitOneTile(unit);
            subtilesMoved--;
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
        int left    = mapBoundsArray[0];
        int top     = mapBoundsArray[1];
        int right   = mapBoundsArray[2];
        int bottom  = mapBoundsArray[3];

		for(int i = top; i < bottom; i++){
			for(int j = left; j < right; j++){
				UnitTile tile = grid.unitGrid[i][j];
				if(tile != null){
					paintUnitTile(graphics, i, j);
				}
			}
		}

	}

	private void paintUnitTile(Graphics2D graphics, int i, int j){
		UnitTile tile = grid.unitGrid[i][j];

		/* Calculate the pixel location of the tile on which we're drawing */
		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();
		int x;
		if(i % 2 == 0)
			x = j * tileX;
		else 
			x = j * tileX + tileX/2;
		int y = i * tileY/2;

		for(UnitLocation loc : UnitLocation.values()) {
			SimpleUnit unit = tile.units[loc.ordinal()];
			if(unit != null && unit.getUnitLocation() == loc)
				paintUnit(graphics, unit, x, y);
		}
	}

    private Point getTileBackLocation(UnitLocation loc){
        int tileBackX, tileBackY;
		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();
        switch(loc){
            case Northwest:
                tileBackX = tileX / 2;
                tileBackY = 0;
                break;
            case West:
                tileBackX = tileX / 3;
                tileBackY = tileY / 6;
                break;
            case North:
                tileBackX = 2 * tileX / 3;
                tileBackY = tileY / 6;
                break;
            case Southwest:
                tileBackX = tileX / 6;
                tileBackY = tileY / 3;
                break;
            case Center:
                tileBackX = tileX / 2;
                tileBackY = tileY / 3;
                break;
            case Northeast:
                tileBackX = 5*tileX / 6;
                tileBackY = tileY / 3;
                break;
            case South:
                tileBackX = tileX / 3;
                tileBackY = tileY / 2;
                break;
            case East:
                tileBackX = 2 * tileX / 3;
                tileBackY = tileY / 2;
                break;
            case Southeast:
                tileBackX = tileX / 2;
                tileBackY = 2 * tileY / 3;
            default:
                tileBackX = -1000;
                tileBackY = -1000;
                break;
        }

        /* TODO: we don't need to create a new point... just have these be private global variables... */
        return new Point(tileBackX, tileBackY);
    }

	private void paintUnit(Graphics2D graphics, SimpleUnit unit, int tileLocX, int tileLocY){
        double percentMovedFromTile = unit.getAnimationCounter();
		int unitBackX = 28, unitBackY = 48;
		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();
        Point backStartSubtile = getTileBackLocation(unit.getUnitLocation());
        Point backShift = Direction.getShift(mapPainter, unit.getDirection());
		int tileBackX = (int)(backStartSubtile.getX()  + percentMovedFromTile * backShift.getX());
		int tileBackY = (int)(backStartSubtile.getY()  + percentMovedFromTile * backShift.getY());


		/* Make the back of the unit agree with the back of the tile */
        int xLoc = tileLocX + tileBackX - unitBackX;
        int yLoc = tileLocY + tileBackY - unitBackY;
        BufferedImage sprite = unit.getCurrentSprite();
		graphics.drawImage(sprite, xLoc, yLoc, null);

        /* Display selected units differently */
        if(unit.isSelected()){
            graphics.setColor(Color.RED);
            graphics.drawRect(xLoc, yLoc, sprite.getWidth(), sprite.getHeight());
        }
	}

    private ArrayList<Shape> unitPolys = new ArrayList<Shape>();
    private ArrayList<SimpleUnit> unitsWithPolys = new ArrayList<SimpleUnit>();
    public SimpleUnit getUnitAtPoint(Point point, Viewport viewport){
        /* Calculate viewport boundaries */
        mapPainter.getViewportTileBounds(mapBoundsArray, viewport);
        int left    = mapBoundsArray[0];
        int top     = mapBoundsArray[1];
        int right   = mapBoundsArray[2];
        int bottom  = mapBoundsArray[3];

        /* Translate the point to be on the map coordinates instead of in screen coordinates */
        point = new Point(point);
        point.translate(viewport.getX(), viewport.getY());

        /* Clear polygons from previous click */
        unitPolys.clear();
        unitsWithPolys.clear();

        /* Loop through visible tiles. Loop backwards, because units in front take precedence. */
        for(int i = bottom - 1; i >= top; i--) {
            for(int j = left; j < right; j++) {
                addVisibleUnitPolysInTile(unitPolys, unitsWithPolys, i, j);
            }
        }

        for(int i = 0; i < unitPolys.size(); i++)
            if(unitPolys.get(i).getBounds().contains(point))
                return unitsWithPolys.get(i);

        return null;
    }

    public SimpleUnit[] getUnitsInRect(Point topLeft, Point bottomRight, Viewport viewport){
        /* Calculate viewport boundaries */
        mapPainter.getViewportTileBounds(mapBoundsArray, viewport);
        int left    = mapBoundsArray[0];
        int top     = mapBoundsArray[1];
        int right   = mapBoundsArray[2];
        int bottom  = mapBoundsArray[3];

        /* Translate the points to be on the map coordinates instead of in screen coordinates */
        topLeft = new Point(topLeft);
        topLeft.translate(viewport.getX(), viewport.getY());
        bottomRight = new Point(bottomRight);
        bottomRight.translate(viewport.getX(), viewport.getY());

        /* Clear polygons from previous click */
        unitPolys.clear();
        unitsWithPolys.clear();

        /* Loop through visible tiles. Loop backwards, because units in front take precedence. */
        for(int i = bottom - 1; i >= top; i--) {
            for(int j = left; j < right; j++) {
                addVisibleUnitPolysInTile(unitPolys, unitsWithPolys, i, j);
            }
        }

        Rectangle rect = new Rectangle(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);

        int count = 0;
        for(int i = 0; i < unitPolys.size(); i++)
            if(rect.intersects(unitPolys.get(i).getBounds()))
                count++;

        SimpleUnit[] selected = new SimpleUnit[count];

        count = 0;
        for(int i = 0; i < unitPolys.size(); i++)
            if(rect.intersects(unitPolys.get(i).getBounds()))
                selected[count++] = unitsWithPolys.get(i);

        return selected;
    }

    public void addVisibleUnitPolysInTile(ArrayList<Shape> polys, ArrayList<SimpleUnit> unitPolys, int i, int j){
		UnitTile tile = grid.unitGrid[i][j];
        
        if(tile == null) return;

		/* Calculate the pixel location of the tile on which we're drawing */
		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();
		int x;
		if(i % 2 == 0)
			x = j * tileX;
		else 
			x = j * tileX + tileX/2;
		int y = i * tileY/2;

		for(UnitLocation loc : UnitLocation.values()) {
			SimpleUnit unit = tile.units[loc.ordinal()];
			if(unit != null && unit.getUnitLocation() == loc)
				addVisibleUnitPoly(polys, unitPolys, unit, x, y);
		}
    }

    private void addVisibleUnitPoly(ArrayList<Shape> polys, ArrayList<SimpleUnit> unitPolys, SimpleUnit unit, int tileLocX, int tileLocY){
        double percentMovedFromTile = unit.getAnimationCounter();
		int unitBackX = 28, unitBackY = 48;
		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();
        Point backStartSubtile = getTileBackLocation(unit.getUnitLocation());
        Point backShift = Direction.getShift(mapPainter, unit.getDirection());
		int tileBackX = (int)(backStartSubtile.getX()  + percentMovedFromTile * backShift.getX());
		int tileBackY = (int)(backStartSubtile.getY()  + percentMovedFromTile * backShift.getY());

		/* Get the image bounding box */
        int width = unit.getCurrentSprite().getWidth();
        int height = unit.getCurrentSprite().getHeight();
		Rectangle boundingBox = new Rectangle(tileLocX + tileBackX - unitBackX, tileLocY + tileBackY - unitBackY, width, height );

        polys.add(boundingBox);
        unitPolys.add(unit);
    }



}
