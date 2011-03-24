package com.scriptrts.game;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;

import com.scriptrts.control.OrderHandler;
import com.scriptrts.core.Main;
import com.scriptrts.game.path.PathHandler;

/**
 * Unit class which only implements most basic functions.
 */
public class GameObject {

    /**
     * The sprites used to display this unit.
     */
    private transient Sprite[] sprites;

    /**
     * Which sprite to display.
     */
    private SpriteState state;

    /**
     * Art image to display in selections
     */
    private transient BufferedImage art;
    
    /**
     * Unit that this game object represents
     */
    private Unit unit;

    /**
     * The direction in which the unit used to be moving. This is used to determine which direction the
     * unit is facing, even when the direction of movement is null (because unit is stationary).
     */
    private Direction previousDirection;

    /**
     * The direction in which the unit is moving.
     */
    private Direction direction;

    /** 
     * The path the unit is taking, defined by a list of directions to follow.
     */
    private Queue<Direction> path = new LinkedList<Direction>();

    /**
     * How far the unit has progressed in its current tile movement.
     */
    private double animCounter = 0;

    /**
     * How far the current sprite animation has progressed (in frames)
     */
    private int spriteAnimationFrameCount = 0;

    /**
     * The shape this unit takes up (which squares it uses)
     */
    private UnitShape shape;


    /**
     * Create a new unit
     * @param p player to whom this unit owes allegiance
     * @param sprites array of sprites used to display the image
     * @param speed unit's speed of movement
     * @param x unit's starting location x coordinate
     * @param y unit's starting location y coordinate
     * @param direction direction in which the unit is facing originally
     * @param shaped if false, this unit takes up one square; if true, it can take up more.
     */
    public GameObject(Player p, Sprite[] sprites, BufferedImage artImg, int speed, int x, int y, Direction direction, boolean shaped, UnitClass unitClass) {
        this.unit = new Unit(p, speed, x, y, new OrderHandler(this), unitClass);
        this.sprites = sprites;
        this.direction = null;
        previousDirection = direction;
        state = SpriteState.Idle;

        if(shaped)
            shape = UnitShape.SHAPE_2x1;
        else
            shape = UnitShape.SHAPE_1x1;

        this.art = artImg;
    }


    /**
     * Create a new unit that takes up one space
     * @param p player to whom this unit owes allegiance
     * @param sprites array of sprites used to display the image
     * @param speed unit's speed of movement
     * @param x unit's starting location x coordinate
     * @param y unit's starting location y coordinate
     * @param direction direction in which the unit is facing originally
     */
    public GameObject(Player p, Sprite[] sprites, BufferedImage artImg, int speed, int x, int y, Direction direction, UnitClass unitClass) {
        this(p, sprites, artImg, speed, x, y, direction, true, unitClass);
    }

    /**
     * Create a blank unit
     */
    public GameObject(){
        this(null, null, null, 0, 0, 0, null, null);
    }

    /**
     * Increment the sprite animation frame count
     */
    public void progressSpriteAnimation(){
        spriteAnimationFrameCount ++;
    }

    /**
     * Reset the sprite animation frame count to zero
     */
    public void resetSpriteAnimation(){
        spriteAnimationFrameCount = 0;
    }

    /**
     * Get the current sprite animation frame
     * @return frame on which the sprite animation is
     */
    public int getSpriteAnimation(){
        return spriteAnimationFrameCount;
    }

    /**
     * Set the sprite animation frame count to a given frame
     * @param f frame to set sprite animation to
     */
    public void resetSpriteAnimation(int f){
        spriteAnimationFrameCount = f;
    }

    /**
     * Set this unit's state to another unit's
     */
    public void setParameters(GameObject sourceObj){
    	Unit source = sourceObj.getUnit();
        unit.setAllegiance(source.getAllegiance());
        unit.setX(source.getX());
        unit.setY(source.getY());
        state = sourceObj.state;
        unit.setId(source.getId());
        unit.setSpeed(source.getSpeed());
        setDirection(sourceObj.direction);
        previousDirection = sourceObj.previousDirection;
        animCounter = 0;
    }

    public void setParameters(Player p, int ux, int uy, SpriteState ustate, int uID, int uspd, Direction udir,
            Direction uPrevDir){
        unit.setAllegiance(p);
        unit.setX(ux);
        unit.setY(uy);
        unit.setId(uID);
        unit.setSpeed(uspd);
        state = ustate;
        setDirection(udir);
        previousDirection = uPrevDir;
        animCounter = 0;
    }
    /**
     * Checks whether this unit can pass over another unit.
     * @param u the unit passing over
     * @return whether the unit can pass over this unit
     */
    public boolean isPassable(GameObject u){
        return false;
    }

    /**
     * Get the art image of this unit.
     * @return image containing art for this unit
     */
    public BufferedImage getArt(){
        return art;
    }



    /**
     * Find how far the animation has progressed
     * @return how many far the animation has progressed (double from 0 to 1)
     */
    public double getAnimationCounter(){
        return animCounter;
    }

    /**
     * Increments the animation counter
     * @param inc how much to increment the counter by
     * @return how many tile movements it finished 
     * */
    public int incrementAnimationCounter(double inc){
        animCounter += inc;

        if(animCounter >= 1){
            int retVal = (int) (animCounter);
            animCounter -= retVal;
            return retVal;
        }

        return 0;

    }

    /**
     * Update the direction based on the path the unit wants to take.
     */
    public void updateDirection(){
        if(direction != null)
            previousDirection = direction;

        /* If we have no more directions, stop. */
        if(path == null || path.peek() == null){
            setDirection(null);
        }
        else {
            setDirection(path.poll());
        }
    }

    /**
     * Look at where the unit will go next without updating the direction
     */
    public Direction peekNextDirection(){
        Direction next;

        if(path == null || path.peek() == null)
            next = null;
        else 
            next = path.peek();

        return next;
    }

    /**
     * Get the path this unit is going to take
     * @return the path this unit will take, as a queue
     */
    public Queue<Direction> getPath(){
        return path;
    }

    /**
     * Set the path this unit is going to take
     * @param path the new path to take
     */
    public void setPath(Queue<Direction> path){

        /* Client */
        if(Main.getGameClient() != null){
            Main.getGameClient().sendPathChangedNotification(this, path);
        } 
        
        /* Server */
        else {
            this.path =  path;

            /* Notify the unit grid that the path has changed */
            Main.getGame().getGameGrid().unitPathChanged(this, path);
        }
        
        this.updateDirection();
    }

    /**
     * Clear the unit path
     */
    public void clearPath(){
        setPath(new LinkedList<Direction>());
    }

    /**
     * Append to the path this unit will take
     * @param d the additional direction to move in
     */
    public void addToPath(Direction d){
            path.add(d);

        if(Main.getGameClient() != null)
            Main.getGameClient().sendPathAppendedNotification(this, d);
    }

    /**
     * Append to the path this unit will take
     * @param additionalPath the additional path to append to the end of the current path
     */
    public void addToPath(Queue<Direction> additionalPath){
        while(additionalPath != null && additionalPath.peek() != null)
            addToPath(additionalPath.poll());
    }

    /**
     * Get the unit ID
     * @return unique id of unit
     */
    public int getID(){
        return unit.getId();
    }

    /**
     * Get the current sprite state
     * @return the sprite state
     */
    public SpriteState getState() {
        return state;
    }

    /**
     * Get the shape of this unit
     * @param facing determines which direction this unit should be facing to get the shape
     * @return an array of points representing offsets from the unit center
     */
    public Point[] getShape(Direction facing){
        return shape.getShape(facing);
    }

    /**
     * Get the current shape of the unit, using the current facing direction.
     * @return an array of points representing offsets from the unit center
     */
    public Point[] getCurrentShape(){
        return getShape(getFacingDirection());
    }

    /**
     * Get the sprites used by this image
     * @return array of sprites this unit may use
     */
    public Sprite[] getSprites(){
        return sprites;
    }

    /**
     * Set the sprite state
     * @param state the state to set
     */
    public void setState(SpriteState state) {
        this.state = state;

        /* Reset sprite animation */
        resetSpriteAnimation();
    }
    
    /**
     * Get the unit represented by this game object
     * @return unit represented by this object
     */
    public Unit getUnit(){
    	return unit;
    }
     



    /**
     * Get all the x coordinates of the current shape
     * @return array of x coordinates
     */
    int[] allX = null;
    public int[] getAllX(){
        if(allX == null)
            allX = new int[shape.getSize()];

        Point[] pts = shape.getShape(getFacingDirection());
        for(int i = 0; i < pts.length; i++)
            allX[i] = pts[i].x + unit.getX();

        return allX;
    }

    /**
     * Get all the y coordinates of the current shape
     * @return array of y coordinates
     */
    int[] allY = null;
    public int[] getAllY(){
        if(allY == null)
            allY = new int[shape.getSize()];

        Point[] pts = shape.getShape(getFacingDirection());
        for(int i = 0; i < pts.length; i++)
            allY[i] = pts[i].y + unit.getY();

        return allY;
    }

    /**
     * Get the direction this unit is moving in
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Get the direction this unit is moving in next
     * @return the direction
     */
    public Direction getNextDirection() {
        return path.peek();
    }

    /**
     * Set the direction this unit is moving in
     * @param direction the direction to set
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * Get the current sprite
     * @return the currentSprite
     */
    public Sprite getCurrentSprite() {
        int index = getFacingDirection().ordinal() + Direction.values().length * getState().ordinal();
        return sprites[index];
    }

    /**
     * Get the direction in which this unit is facing
     * @return direction this unit is facing in
     */
    public Direction getFacingDirection(){
        if(direction != null)
            return direction;
        else
            return previousDirection;
    }
    
    /**
     * Check if two game objects are equal by comparing their units' ids.
     * @param obj GameObject to compare to this one
     * @return true if the game objects are the same unit
     */
    public boolean equals(GameObject obj){
    	return obj.getUnit().equals(this.getUnit());
    }
    
    /**
     * Check if the game object is movable, i.e. is not a building or terrain
     * @return true if game object is movable
     */
    public boolean isMovable(){
    	UnitClass unitClass = unit.getUnitClass();
    	if(unitClass == UnitClass.Building || unitClass == UnitClass.Terrain)
    		return false;
    	return true;
    }
}
