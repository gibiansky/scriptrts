package com.scriptrts.core;

import java.awt.*;
import com.scriptrts.util.ResourceManager;
import com.scriptrts.game.*;
import com.scriptrts.combat.*;

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
			/* Initialize the rider at the middle of the terrain tile (5,5), facing E.
			 *(Direction, at the moment, doesn't change. */
			SimpleUnit rider = new SimpleUnit(ResourceManager.loadImage("resource/unit/rider/FemaleRider.png"),
					1, 5, 5, UnitLocation.C, Direction.East);
			/* Place the rider in the unit tile */
			tile.units[0] = rider;
			/* Put the unit tile in the UnitGrid (to be associated with terrain tiles)*/
			grid.unitGrid[5][5] = tile;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Paints all visible units onto the screen
	 */
	public void paintUnits(Graphics2D graphics, Viewport viewport){
		/* Calculate the viewport edges (in map tiles) */
		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();

		/* Copied and pasted from MapPainter. TODO: make a method in map painter to do this.*/
		int left = (int) (viewport.getX() / tileX) - 1;
		int top = (int) (viewport.getY() / (tileY / 2)) - 1;
		if(left < 0) left = 0;
		if(top < 0) top = 0;

		int right = (int) ((viewport.getX() + viewport.getWidth()) / tileX) + 1;
		int bottom = (int) ((viewport.getY() + viewport.getHeight()) / (tileY / 2)) + 1;
		if(right > mapPainter.getMap().getN()) right = mapPainter.getMap().getN();
		if(bottom > mapPainter.getMap().getN()) bottom = mapPainter.getMap().getN();

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

		//		for(int subtile = 0; subtile < SPACES_PER_TILE * SPACES_PER_TILE; subtile++){
		//			SimpleUnit unit = tile.units[subtile];
		//			if(unit != null && unit.unitLocation == subtile){
		//				paintUnit(graphics, unit, x, y);
		//			}
	}

	private void paintUnit(Graphics2D graphics, SimpleUnit unit, int tileLocX, int tileLocY){
		int unitBackX = 28, unitBackY = 48;
		int tileX = mapPainter.getTileWidth();
		int tileY = mapPainter.getTileHeight();
		int tileBackX = 0, tileBackY = 0;
		switch(unit.getUnitLocation()){
		case SW:
			tileBackX = tileX / 2;
			tileBackY = 0;
			break;
		case W:
			tileBackX = tileX / 3;
			tileBackY = tileY / 6;
			break;
		case S:
			tileBackX = 2 * tileX / 3;
			tileBackY = tileY / 6;
			break;
		case NW:
			tileBackX = tileX / 6;
			tileBackY = tileY / 3;
			break;
		case C:
			tileBackX = tileX / 2;
			tileBackY = tileY / 3;
			break;
		case SE:
			tileBackX = 5*tileX / 6;
			tileBackY = tileY / 3;
			break;
		case N:
			tileBackX = tileX / 3;
			tileBackY = tileY / 2;
			break;
		case E:
			tileBackX = 2 * tileX / 3;
			tileBackY = tileY / 2;
			break;
		case NE:
			tileBackX = tileX / 2;
			tileBackY = 2 * tileY / 3;
		default:
			break;

		}

		/* Make the back of the unit agree with the back of the tile */
		graphics.drawImage(unit.getSprite(), tileLocX + tileBackX - unitBackX, tileLocY + tileBackY - unitBackY, null);
		graphics.setColor(Color.blue);
	}

}
