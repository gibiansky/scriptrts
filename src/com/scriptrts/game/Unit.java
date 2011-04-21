package com.scriptrts.game;

import java.awt.Point;

import com.scriptrts.control.OrderHandler;
import com.scriptrts.core.Main;
import com.scriptrts.game.path.PathHandler;
import com.scriptrts.game.path.Pathfinder;


/**
 * Unit object for units on the map
 */
public class Unit extends UnitType {
    /**
     * Unit ID counter
     */
    private static int idCounter = 0;

    /**
     * Unit ID
     */
    private int id;

    /**
     * The unit's movement speed
     */
    private int speed;

    /**
     * The unit's location on the map 
     */
    private int x, y;

    /**
     * Player who controls this unit
     */
    private Player player;
    /**
     * Where this unit is going, in unit tile coordinates
     */
    private Point destination;

    /**
     * The current remaining hitpoints of the unit
     */
    private int health;

    /**
     * Whether the unit is alive
     */
    private boolean alive = true;

    /**
     * Path handler used to route this unit
     */
    private PathHandler pathHandler;

    /**
     * The OrderHandler used to control this unit
     */
    private transient OrderHandler orderHandler;

    /**
     * The sight radius of the unit
     */
    private int visibilityRadius;
    
    /**
     * Create a new unit
     */
    public Unit(Player p, int s, int x, int y, OrderHandler orderHandler, UnitClass unitClass){
    	super();
    	this.player = p;
    	this.speed = s;
        this.x = x;
        this.y = y;
    	this.orderHandler = orderHandler;
    	this.unitClass = unitClass;
    	pathHandler = Main.getGame().getPathHandler();
        this.id = idCounter;
        idCounter++;
        
        hitpoints = 10;
        health = (int) (Math.random() * hitpoints);
        visibilityRadius = 3;
    }
    
    /**
     * Check if this unit is the same unit as another one, based on ID.
     * @param unit unit to check against
     * @return true if the unit is the same, false otherwise
     */
    public boolean equals(Unit unit){
        return unit.id == id;
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

        GameObject gameObj = Main.getGame().getGameManager().unitWithId(getId());
        if(gameObj.getPath() != null){
        	gameObj.setDirection(null);
        }

        if(!pathHandler.isEmpty()){
            Pathfinder pathfinder = pathHandler.remove();
            pathfinder.setUnit(gameObj);
            pathfinder.setDestination(p.x, p.y);
            Thread runner = new Thread(pathfinder);
            runner.setPriority(Thread.MIN_PRIORITY);
            runner.start();
        } else
            pathHandler.addPath(gameObj, p);
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
     * Get the unit id
     * @return unit id
     */
    public int getId(){
    	return id;
    }
    
    /**
     * Set the unit id
     * @param i new id
     */
    public void setId(int i){
    	this.id = i;	
    }

    public void setOrderHandler(OrderHandler orderhandler) {
        this.orderHandler = orderhandler;
    }

    public OrderHandler getOrderHandler() {
        return orderHandler;
    }

    public boolean isPassable(Unit c) {
        return false;
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
     * Returns the visible tiles for this unit's visibility radius about the point x, y
     */
    public Point[] getVisibleTiles(int x, int y){
    	Point[] visibleTiles = new Point[(int) Math.pow(2 * visibilityRadius + 1, 2)];
    	int count = 0;
    	for(int i = x - visibilityRadius; i <= x + visibilityRadius; i++){
    		for(int j = y - visibilityRadius; j <= y + visibilityRadius; j++){
    			if(Main.getGame().getGameGrid().contains(i, j))
    				visibleTiles[count] = new Point(i, j);
    			count++;
    		}
    	}
    	return visibleTiles;
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
     * Get the player who currently controls this unit
     */
    public Player getAllegiance(){
        return player;
    }
    
    /**
     * Set the player who currently controls this unit
     */
    public void setAllegiance(Player p){
        player = p;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return alive;
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
     * Get the location of the unit
     * @return Point p -- the location of this unit
     */
    public Point getLocation(){
        return new Point(x, y);
    }
}
