package com.scriptrts.game;

import java.awt.image.BufferedImage;

public class SimpleUnit {

	public BufferedImage sprite;
	private SpriteState state;
	private int speed;
	private int x, y;
    public int unitLocation;
	private Direction direction;
	
	/**
	 * @param sprite
	 * @param speed
	 * @param x
	 * @param y
	 */
	public SimpleUnit(BufferedImage sprite, int speed, int x, int y, Direction direction) {
		this.sprite = sprite;
		this.speed = speed;
		this.x = x;
		this.y = y;
		this.direction = direction;
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
	
}
