package com.scriptrts.game;

import java.awt.image.BufferedImage;

import com.scriptrts.core.UnitLocation;

public class SimpleUnit {

	private BufferedImage[] sprites;
	private BufferedImage currentSprite;
	private SpriteState state;
	private int speed;
	private int x, y;
    public UnitLocation unitLocation, dLoc;
	private Direction direction;
    private boolean selected;
	
	public SimpleUnit(BufferedImage[] sprites, int speed, int x, int y, UnitLocation unitLocation, Direction direction) {
		this.sprites = sprites;
		this.speed = speed;
		this.x = x;
		this.y = y;
		this.unitLocation = unitLocation;
		this.direction = direction;
		state = SpriteState.Idle;
		this.setCurrentSprite(sprites[SpriteState.Idle.ordinal()]);
	}

    public boolean isSelected(){
        return selected;
    }
    public void select(){
        selected = true;
    }
    public void deselect(){
        selected = false;
    }

    private double animCounter = 0;
    public double getAnimationCounter(){
        return animCounter;
    }
    /* returns how many tile movements it finished */
    public int incrementAnimationCounter(double inc){
        animCounter += inc;

        if(animCounter >= 1){
            int retVal = (int) (animCounter);
            animCounter -= retVal;
            return retVal;
        }

        return 0;

    }
    public void resetAnimationCounter(){
        animCounter = 0;
    }



	public void move() {
		this.dLoc = UnitLocation.East;
		if(unitLocation != dLoc) {
			state = SpriteState.Moving;
			currentSprite = sprites[state.ordinal()];
		}
	}
	
	public void idle() {
		state = SpriteState.Idle;
		currentSprite = sprites[state.ordinal()];
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
	public BufferedImage[] getSprites() {
		return sprites;
	}

	/**
	 * @param sprite the sprite to set
	 */
	public void setSprites(BufferedImage[] sprites) {
		this.sprites = sprites;
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

	/**
	 * @param currentSprite the currentSprite to set
	 */
	public void setCurrentSprite(BufferedImage currentSprite) {
		this.currentSprite = currentSprite;
	}

	/**
	 * @return the currentSprite
	 */
	public BufferedImage getCurrentSprite() {
		return currentSprite;
	}
}