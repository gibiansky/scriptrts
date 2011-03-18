package com.scriptrts.game;

import java.awt.image.BufferedImage;
import java.awt.Point;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.io.Serializable;

import com.scriptrts.core.PathHandler;
import com.scriptrts.core.Pathfinder;
import com.scriptrts.core.Main;
import com.scriptrts.game.Unit;
import com.scriptrts.control.OrderHandler;

/**
 * Unit class which only implements most basic functions.
 */
public class SimpleUnit implements Serializable {

    /**
     * The sprites used to display this unit.
     */
    private transient Sprite[] sprites;

    /**
     * Which sprite to display.
     */
    private SpriteState state;

    /**
     * The unit's movement speed
     */
    private int speed;

    /**
     * The unit's location on the map 
     */
    private int x, y;

    /**
     * Art image to display in selections
     */
    private transient BufferedImage art;

    /**
     * Player who controls this unit
     */
    private Player player;

    /**
     * Unit ID
     */
    private int id;

    /**
     * Unit ID counter
     */
    private static int idCounter = 0;

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
     * Where this unit is going, in unit tile coordinates
     */
    private Point destination;

    /**
     * The maximum health the unit can have
     */
    private int maxHealth;

    /**
     * The current health of the unit
     */
    private int health;

    /**
     * Whether the unit is alive
     */
    private boolean alive = true;

    /**
     * Path handler used to route this unit
     */
    private transient PathHandler pathHandler;

    /**
     * The OrderHandler used to control this unit
     */
    private transient OrderHandler orderHandler;

    /**
     * Unit type that this unit corresponds to
     */
    private Unit unitType;

    /**
     * The sight radius of the unit
     */
    private int visibilityRadius;

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
    public SimpleUnit(Player p, Sprite[] sprites, BufferedImage artImg, int speed, int x, int y, Direction direction, boolean shaped, PathHandler pathHandler) {
        this.player = p;
        this.sprites = sprites;
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.direction = null;
        this.pathHandler = pathHandler;
        this.id = idCounter;
        idCounter++;

        this.orderHandler = new OrderHandler(this);

        previousDirection = direction;
        state = SpriteState.Idle;

        if(shaped)
            shape = UnitShape.SHAPE_2x1;
        else
            shape = UnitShape.SHAPE_1x1;

        this.art = artImg;

        maxHealth = 10;
        health = (int) (Math.random() * maxHealth);

        visibilityRadius = 2;
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
    public SimpleUnit(Player p, Sprite[] sprites, BufferedImage artImg, int speed, int x, int y, Direction direction, PathHandler pathHandler) {
        this(p, sprites, artImg, speed, x, y, direction, true, pathHandler);
    }

    /**
     * Returns the sight radius of the unit
     * @return the sight radius
     */     
    public int getVisibilityRadius() {
        return visibilityRadius;
    }

    /**
     * Sets the sight radius of the unit
     * @param the new radius
     */
    public void setVisibilityRadius(int radius) {
        visibilityRadius = radius;
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
     * Get the maximum health of the unit
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Get the current health of the unit
     */
    public int getHealth() {
        return health;
    }

    /**
     * Set the current health of the unit
     * @param health the health to set
     */
    public void setHealth(int health) {
        this.health = health;
    }

    /**
     * Set the maximum health of the unit
     * @param maxHealth the maximum health to set
     */
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }


    /**
     * Get the player who currently controls this unit
     */
    public Player getAllegiance(){
        return player;
    }

    /**
     * Set this unit's state to another unit's
     */
    public void setParameters(SimpleUnit source){
        player = source.player;
        x = source.x;
        y = source.y;
        state = source.state;
        id = source.id;
        speed = source.speed;
        direction = source.direction;
        previousDirection = source.previousDirection;
        animCounter = source.animCounter;
        animCounter = 0;
    }

    /**
     * Checks whether this unit can pass over another unit.
     * @param u the unit passing over
     * @return whether the unit can pass over this unit
     */
    public boolean isPassable(SimpleUnit u){
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
     * Find where this unit is going
     * @return unit destination
     */
    public Point getDestination() {
        return destination;
    }

    /**
     * Set the unit destination
     * @param p new unit destination
     */
    public void setDestination(Point p){
        destination = p;

        if(path != null){
            direction = null;
            clearPath();
        }
        if(!pathHandler.isEmpty()){
            Pathfinder pathfinder = pathHandler.remove();
            pathfinder.setUnit(this);
            pathfinder.setDestination(p.x, p.y);
            Thread runner = new Thread(pathfinder);
            runner.setPriority(Thread.MIN_PRIORITY);
            runner.start();
        } else
            pathHandler.addPath(this, p);
    }

    /**
     * Set the unit destination
     * @param x new unit destination x coordinate
     * @param y new unit destination y coordinate
     */

    public void setDestination(int x, int y){
        setDestination(new Point(x, y));
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
            direction = null;
        }
        else {
            direction = path.poll();
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
        return (Queue<Direction>) Collections.unmodifiableCollection(path);
    }

    /**
     * Set the path this unit is going to take
     * @param path the new path to take
     */
    public void setPath(Queue<Direction> path){
        this.path =  path;
        if(direction == null && path != null && path.peek() != null)
            direction = path.poll();

        Main.getGameClient().sendPathChangedNotification(this, path);
    }

    /**
     * Clear the unit path
     */
    public void clearPath(){
        path.clear();

        Main.getGameClient().sendPathChangedNotification(this, path);
    }

    /**
     * Append to the path this unit will take
     * @param d the additional direction to move in
     */
    public void addToPath(Direction d){
        if(direction == null)
            direction = d;
        else
            path.add(d);

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
        return id;
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
     * Get the unit speed of movement
     * @return the speed
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * Set the unit speed of movement
     * @param speed the speed to set
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * Get x coordinate of location
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * Set x coordinate of location
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Get y coordinate of location
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * Set y coordinate of location
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
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
            allX[i] = pts[i].x + getX();

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
            allY[i] = pts[i].y + getY();

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
     * Check if this unit is the same unit as another one, based on ID.
     * @param unit unit to check against
     * @return true if the unit is the same, false otherwise
     */
    public boolean equals(SimpleUnit unit){
        return unit.id == id;
    }

    public void setOrderhandler(OrderHandler orderhandler) {
        this.orderHandler = orderhandler;
    }

    public OrderHandler getOrderHandler() {
        return orderHandler;
    }

    public boolean isPassable(Construct c) {
        return false;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return alive;
    }
}

/**
 * The shape of the unit, defined by what points it takes up 
 * around the center point.
 */
class UnitShape implements Serializable {
    /**
     * An array of point arrays, with each element being an array that corresponds to the shape
     * of the unit when it is facing in each of the 8 directions.
     */
    Point[][] shapes = new Point[8][];

    /**
     * A two-by-one elongated unit shape
     */
    static final UnitShape SHAPE_2x1 = new UnitShape(
            new Point[][]{
                /* North */
                new Point[]{new Point(0,0), new Point(0, -1)},
                 /* Northeast */
                 new Point[]{new Point(0,0), new Point(-1, -1)},
                 /* East */
                 new Point[]{new Point(0,0), new Point(-1, 0)},
                 /* Southeast */
                 new Point[]{new Point(0,0), new Point(-1, 1)},
                 /* South */
                 new Point[]{new Point(0,0), new Point(0, 1)},
                 /* Southwest */
                 new Point[]{new Point(0,0), new Point(1, 1)},
                 /* West */
                 new Point[]{new Point(0,0), new Point(1, 0)},
                 /* Northwest */
                 new Point[]{new Point(0,0), new Point(1, -1)}
            }
            );

    /**
     * A simple shape composed of one square
     */
    static final UnitShape SHAPE_1x1 = new UnitShape(
            new Point[][]{
                /* North */
                new Point[]{new Point(0,0)},
                 /* Northeast */
                 new Point[]{new Point(0,0)},
                 /* East */
                 new Point[]{new Point(0,0)},
                 /* Southeast */
                 new Point[]{new Point(0,0)},
                 /* South */
                 new Point[]{new Point(0,0)},
                 /* Southwest */
                 new Point[]{new Point(0,0)},
                 /* West */
                 new Point[]{new Point(0,0)},
                 /* Northwest */
                 new Point[]{new Point(0,0)}
            }
            );

    /**
     * Create a new unit shape
     * @param shps the points in the shape, with each point being an offset from the unit center
     */
    public UnitShape(Point[][] shps){
        super();
        shapes = shps;
    }

    /**
     * Get the shape of the unit when it is facing a given direction
     * @param facing which direction it's facing
     * @return point array denoting unit shape
     */
    public Point[] getShape(Direction facing){
        return shapes[facing.ordinal()];
    }

    /**
     * Get how many squares this unit takes up
     * @return how many squares this unit takes up when facing North
     */
    public int getSize(){
        return getShape(Direction.North).length;
    }
}
