package com.scriptrts.util;

import java.awt.Point;
import java.util.ArrayList;

import com.scriptrts.game.Direction;
import com.scriptrts.game.GameObject;
import com.scriptrts.game.Sprite;
import com.scriptrts.game.SpriteState;

/**
 * Helper class to store information about loaded sprites, images, and unit shapes
 * Used by SpriteLoader to add new sprites
 */
public class SpriteHelper {
	
	/**
	 * Unit to generate sprite sheet and determine unit shape for
	 */
	private GameObject unit;
	
	/**
	 * Coordinates of the unit tile the unit is on
	 */
	private int tileX, tileY;
	
	/**
	 * The unit's shape, 8 arraylists of points, 1 for each direction
	 */
	private ArrayList[] unitShape;
	
	/**
	 * The unit's image file locations, an arraylist of strings for every direction and sprite state
	 */
	private ArrayList[] imageFiles;
	
	/**
	 * The unit's sprites, 1 for every direction and sprite state 
	 */
	private Sprite[] sprites;
	
	/**
	 * The number of sprite states in the enum
	 */
	private int nStates = SpriteState.values().length;
	
	/**
	 * Create a new sprite helper
	 */
	public SpriteHelper(){
		unit = null;
		tileX = 0;
		tileY = 0;
		unitShape = new ArrayList[8];
		for(int i = 0; i < 8; i++)
			unitShape[i] = new ArrayList<Point>();
		imageFiles = new ArrayList[8 * nStates];
		for(int i = 0; i < imageFiles.length; i++)
			imageFiles[i] = new ArrayList<String>();
		sprites = new Sprite[8 * nStates];
	}
	
	/**
	 * Add a sprite for the unit
	 * @param s sprite to add
	 * @param index index that uniquely identifies the direction and sprite state for this sprite,
	 * given by direction.ordinal() + 8 * spriteState.ordinal() for standard units, or spriteState.ordinal()
	 * for buildings and terrain objects
	 */
	public void addSprite(Sprite s, int index){
		sprites[index] = s;
	}
	
	/**
	 * Add an image file location for the unit
	 * @param file file location to add
	 * @param index index that uniquely identifies the direction and sprite state for this sprite,
	 * given by direction.ordinal() + 8 * spriteState.ordinal() for standard units, or spriteState.ordinal()
	 * for buildings and terrain objects
	 * @param frameNumber frameNumber in animation (default 1 if it is a static sprite)
	 */
	public void addImageFile(String file, int index, int frameNumber){
		//System.out.println(index + " " + frameNumber + " " + imageFiles.length + " " + imageFiles[index].size());
		if(frameNumber > imageFiles[index].size()){
			for(int i = imageFiles[index].size(); i < frameNumber; i++)
				imageFiles[index].add("");
		}
		imageFiles[index].set(frameNumber - 1, file);
	}
	
	/**
	 * Add a square to the unit's shape
	 * @param i unit tile x coordinate to add
	 * @param j unit tile y coordinate to add
	 * @param facing direction the unit is currently facing
	 */
	public void addSquare(int i, int j, Direction facing){
		unitShape[facing.ordinal()].add(new Point(i - tileX, j - tileY));
	}
	
	/**
	 * Allocate the total number of sprites needed, given by 8 * nStates for standard units, or nStates for
	 * buildings and terrain objects
	 * @param n number of sprites to allocate
	 */
	public void setNumberOfSprites(int n){
		sprites = new Sprite[n];
		imageFiles = new ArrayList[n];
		for(int i = 0; i < n; i++)
			imageFiles[i] = new ArrayList<String>();
	}
	
	/**
	 * Whether or not the unit's shape in the direction it's currently facing contains the given point
	 * @param p point to check
	 * @return whether the unit's shape in the direction it's currently facing contains p
	 */
	private boolean contains(Point p){
		return unitShape[this.unit.getFacingDirection().ordinal()].contains(p);
	}
	
	/**
	 * Whether or not the unit's shape in the direction it's currently facing contains (i,j)
	 * @param i unit tile x coordinate to check
	 * @param j unit tile y coordinate to check
	 * @return whether the unit's shape in the direction it's currently facing contains (i,j)
	 */
	public boolean contains(int i, int j){
		return this.contains(new Point(i - tileX, j - tileY));
	}
	
	/**
	 * Number of unit tiles added to the unit's current shape
	 * @return number of unit tiles in the unit's shape in the current facing direction
	 */
	public int size(){
		return unitShape[this.unit.getFacingDirection().ordinal()].size();
	}
	
	/**
	 * Get the unit this SpriteHelper is for
	 * @return unit currently being checked
	 */
	public GameObject getUnit(){
		return unit;
	}

	/**
	 * Set the unit this SpriteHelper is for 
	 * @param unit unit to set
	 */
	public void setUnit(GameObject unit){
		this.unit = unit;
	}

	/**
	 * Get the unit's current unit tile x coordinate
	 * @return unit's current unit tile x coordinate
	 */
	public int getTileX() {
		return tileX;
	}

	/**
	 * Set the unit's unit tile x coordinate
	 * @param tileX unit tile x coordinate to set
	 */
	public void setTileX(int tileX) {
		this.tileX = tileX;
	}

	/**
	 * Get the unit's current unit tile y coordinate
	 * @return unit's current unit tile y coordinate
	 */
	public int getTileY() {
		return tileY;
	}

	/**
	 * Set the unit's unit tile y coordinate
	 * @param tileY unit tile y coordinate to set
	 */
	public void setTileY(int tileY) {
		this.tileY = tileY;
	}

	/**
	 * Get the unit's shape in the current facing direction
	 * @return unit's shape in the current facing direction
	 */
	public ArrayList<Point> getUnitShape(){
		return unitShape[this.unit.getFacingDirection().ordinal()];
	}
	
	/**
	 * Get the unit's sprite array
	 * @return unit's sprite array
	 */
	public Sprite[] getSprites(){
		return sprites;
	}
	
	/**
	 * Get the unit's image file location array
	 * @return unit's image file location array
	 */
	public ArrayList[] getImageFiles(){
		return imageFiles;
	}
}
