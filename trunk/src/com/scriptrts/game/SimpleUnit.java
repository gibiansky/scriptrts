package com.scriptrts.game;

import java.awt.image.BufferedImage;

import com.scriptrts.core.UnitLocation;

public class SimpleUnit {

	private BufferedImage sprite;
	private SpriteState state;
	private int speed;
	private int x, y;
    private UnitLocation unitLocation, dLoc;
	private Direction direction;
	
	public SimpleUnit(BufferedImage sprite, int speed, int x, int y, UnitLocation unitLocation, Direction direction) {
		this.sprite = sprite;
		this.speed = speed;
		this.x = x;
		this.y = y;
		this.unitLocation = unitLocation;
		this.direction = direction;
	}

	public void move() {
		this.dLoc = UnitLocation.East;
		if(unitLocation != dLoc)
			state = SpriteState.Moving;
	}
	
	/**
	 * @return the state
	 */
	public SpriteState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(SpriteState state) {
		this.state = state;
	}

	/**
	 * @return the speed
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @return the direction
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * @param direction the direction to set
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	/**
	 * @return the sprite
	 */
	public BufferedImage getSprite() {
		return sprite;
	}

	/**
	 * @param sprite the sprite to set
	 */
	public void setSprite(BufferedImage sprite) {
		this.sprite = sprite;
	}

	/**
	 * @return the unitLocation
	 */
	public UnitLocation getUnitLocation() {
		return unitLocation;
	}

	/**
	 * @param unitLocation the unitLocation to set
	 */
	public void setUnitLocation(UnitLocation unitLocation) {
		this.unitLocation = unitLocation;
	}
	
}
