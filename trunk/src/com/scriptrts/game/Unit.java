package com.scriptrts.game;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Queue;

import com.scriptrts.control.OrderHandler;
import com.scriptrts.core.Main;
import com.scriptrts.core.PathHandler;
import com.scriptrts.core.Pathfinder;
import com.scriptrts.core.ui.Sprite;


/**
 * Unit object for unit types
 */
public class Unit extends Construct {
    
    /**
     * The unit's movement speed
     */
    private int speed;
    
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
     * The unit type name
     */
    private String name;

    /**
     * Number of hitpoints unit type starts with
     */
    private int hitpoints;

    /**
     * The armor bonus of this unit
     */
    private int armor;

    /**
     * The base attack of this unit
     */
    private int attack;



    /**
     * Where this unit is going, in unit tile coordinates
     */
    private Point destination;
    
    /**
     * Path handler used to route this unit
     */
    private transient PathHandler pathHandler;

    /**
     * The OrderHandler used to control this unit
     */
    private transient OrderHandler orderHandler;
    

    /**
     * Create a new unit
     */
    public Unit(Player p, Sprite[] sprites, BufferedImage artImg, int speed, int x, int y, Direction direction, boolean shaped, PathHandler pathHandler){
        mapObject = new MapObject(sprites, artImg, shaped, this);
        
        this.player = p;
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.direction = null;
        this.pathHandler = pathHandler;
        
        this.orderHandler = new OrderHandler(this);
        
        previousDirection = direction;
        
        maxHealth = 10;
        health = (int) (Math.random() * maxHealth);

        visibilityRadius = 2;
    }

    /**
     * Create an empty, uninitialized unit
     */
    public Unit(){
        this(null, null, null, 0, 0, 0, null, false, null);
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
       
	@Override
    public boolean isPassable(Construct c) {
        return false;
    }
     
     public int getID(){
         return mapObject.getID();
     }
     
     /**
      * Set this unit's state to another unit's
      */
     public void setParameters(Unit source){
         player = source.player;
         x = source.x;
         y = source.y;
         this.mapObject.setParameters(source.mapObject);
         speed = source.speed;
         direction = source.direction;
         previousDirection = source.previousDirection;
     }
     

      /**
      * Set unit state
      * @param p allegiance player
      * @param ux unit x location
      * @param uy unit y location
      * @param spriteState sprite state of the unit
      * @param id unit id
      * @param uspd unit speed
      * @param udir unit direction
      * @param uPrevDir previous unit direction
      */
	public void setParameters(Player p, int ux, int uy, SpriteState spriteState, int id, int uspd, Direction udir, Direction uPrevDir) {
		player = p;
        x = ux;
        y = uy;
        speed = uspd;
        direction = udir;
        previousDirection = uPrevDir;
        
        getMapObject().setParameters(spriteState, id);
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
         return path;
     }

     /**
      * Set the path this unit is going to take
      * @param path the new path to take
      */
     public void setPath(Queue<Direction> path){
         this.path =  path;
         if(Main.getGameClient() != null)
             Main.getGameClient().sendPathChangedNotification(this, path);

         /* Notify the unit grid that the path has changed */
         Main.getGame().getUnitGrid().unitPathChanged(this, path);

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
      * Get the direction this unit is moving in next after it finishes the current movement
      * @return the direction
      */
     public Direction getSubsequentDirection() {
         LinkedList<Direction> pth = (LinkedList<Direction>) path;
         if(pth.size() < 2)
             return null;
         else
             return pth.get(1);
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
      * Set the direction this unit is moving in
      * @param direction the direction to set
      */
     public void setDirection(Direction direction) {
         this.direction = direction;
     }
     
     public void setOrderhandler(OrderHandler orderhandler) {
         this.orderHandler = orderhandler;
     }

     public OrderHandler getOrderHandler() {
         return orderHandler;
     }

    /**
     * Check if the units are equal
     * @return true if the unit is the same instance
     */
    public boolean equals(Unit u){
        return u.getMapObject().getID() == getMapObject().getID();
    }

    /**
     * Get the name of the unit type
     * @return unit type name
     */
    public String name(){
        return name;
    }


    /**
     * Get the armor bonus
     * @return armor
     */
    public int armor(){
        return armor;
    }

    /**
     * Get the base attack
     * @return base attack
     */
    public int attack(){
        return attack;
    }
}
