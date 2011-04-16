package com.scriptrts.util;

import java.awt.Point;
import java.util.ArrayList;

import com.scriptrts.game.Direction;
import com.scriptrts.game.GameObject;

public class SpriteHelper {
	
	private GameObject unit;
	private int tileX, tileY;
	private int coordX, coordY;
	private ArrayList[] unitShape;
	
	public SpriteHelper(){
		unit = null;
		tileX = 0;
		tileY = 0;
		unitShape = new ArrayList[8];
		for(int i = 0; i < 8; i++)
			unitShape[i] = new ArrayList<Point>();
	}
	
	public SpriteHelper(GameObject unit){
		this.unit = unit;
		tileX = unit.getUnit().getX();
		tileY = unit.getUnit().getY();
		unitShape = new ArrayList[8];
		for(int i = 0; i < 8; i++)
			unitShape[i] = new ArrayList<Point>();
	}
	
	public void addSquare(int i, int j, Direction facing){
		unitShape[facing.ordinal()].add(new Point(i - tileX, j - tileY));
	}
	
	private boolean contains(Point p){
		return unitShape[this.unit.getFacingDirection().ordinal()].contains(p);
	}
	
	public boolean contains(int i, int j){
		return this.contains(new Point(i - tileX, j - tileY));
	}
	
	public int size(){
		return unitShape[this.unit.getFacingDirection().ordinal()].size();
	}
	
	public GameObject getUnit(){
		return unit;
	}

	public void setUnit(GameObject unit){
		this.unit = unit;
	}

	public int getTileX() {
		return tileX;
	}

	public void setTileX(int tileX) {
		this.tileX = tileX;
	}

	public int getTileY() {
		return tileY;
	}

	public void setTileY(int tileY) {
		this.tileY = tileY;
	}
	
	public int getCoordX() {
		return coordX;
	}

	public void setCoordX(int coordX) {
		this.coordX = coordX;
	}

	public int getCoordY() {
		return coordY;
	}

	public void setCoordY(int coordY) {
		this.coordY = coordY;
	}

	public ArrayList<Point> getUnitShape(){
		return unitShape[this.unit.getFacingDirection().ordinal()];
	}
}
