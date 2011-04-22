package com.scriptrts.game.path;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

import com.scriptrts.game.GameObject;
import com.scriptrts.game.GameMap;
import com.scriptrts.game.MapGrid;


public class PathHandler{
	
	/**
	 * Queue of pathfinders that units can call upon when they need to find a route
	 */
	private Queue<Pathfinder> pathfinders;
	
	/**
	 * Overflow of units to route, if there are more units to route than pathfinders available
	 */
	private Queue<GameObject> unitsToRoute;
	
	/**
	 * Overflow of corresponding destinations
	 */
	private Queue<Point> destinations;
	
	/**
	 * Create a new path handler
	 */
	public PathHandler(){
		pathfinders = new LinkedList<Pathfinder>();
		unitsToRoute = new LinkedList<GameObject>();
		destinations = new LinkedList<Point>();
	}
	
	/**
	 * Set the number of pathfinders that units can use
	 * @param n number of pathfinders
	 */
	public void setNumPathfinders(int n){
		for(int i = 0; i < n; i++){
			Pathfinder finder = new Pathfinder();
			finder.setPathHandler(this);
			pathfinders.add(finder);
		}		
	}
	
	/**
	 * Whether or not there are any more available pathfinders
	 * @return whether or not there are any more available pathfinders for units to use
	 */
	public boolean isEmpty(){
		return pathfinders.isEmpty();
	}
	
	/**
	 * Add a pathfinder
	 * @param pathfinder pathfinder to add
	 */
	public void add(Pathfinder pathfinder){
		pathfinders.add(pathfinder);
	}
	
	/**
	 * Remove the first available (not currently being used) pathfinder
	 * @return first available pathfinder
	 */
	public Pathfinder remove(){
		return pathfinders.poll();
	}
	
	/**
	 * Add an overflow path to route
	 * @param unit unit to route path for
	 * @param end destination for unit
	 */
	public void addPath(GameObject unit, Point end){
		unitsToRoute.add(unit);
		destinations.add(end);
	}
	
	/**
	 * Update the path handler to route next overflow route
	 */
	public void update(){
		if(!this.isEmpty() && !unitsToRoute.isEmpty()){
			GameObject unit = unitsToRoute.poll();
			Point end = destinations.poll();
			unit.getUnit().setDestination(end);
		}
	}
}
