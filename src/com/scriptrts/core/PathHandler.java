package com.scriptrts.core;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

import com.scriptrts.game.SimpleUnit;
import com.scriptrts.game.UnitGrid;

public class PathHandler{
	
	/**
	 * Queue of pathfinders that units can call upon when they need to find a route
	 */
	private Queue<Pathfinder> pathfinders;
	
	/**
	 * Current map instance
	 */
	private Map map;
	
	/**
	 * Current unit grid instance
	 */
	private UnitGrid unitGrid;
	
	/**
	 * Overflow of units to route, if there are more units to route than pathfinders available
	 */
	private Queue<SimpleUnit> unitsToRoute;
	
	/**
	 * Overflow of corresponding destinations
	 */
	private Queue<Point> destinations;
	
	/**
	 * Create a new path handler
	 * @param map current map instance
	 * @param unitGrid current unit grid instance
	 */
	public PathHandler(Map map, UnitGrid unitGrid){
		pathfinders = new LinkedList<Pathfinder>();
		this.map = map;
		this.unitGrid = unitGrid;
		unitsToRoute = new LinkedList<SimpleUnit>();
		destinations = new LinkedList<Point>();
	}
	
	/**
	 * Set the number of pathfinders that units can use
	 * @param n number of pathfinders
	 */
	public void setNumPathfinders(int n){
		for(int i = 0; i < n; i++){
			Pathfinder finder = new Pathfinder(map, unitGrid);
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
	public void addPath(SimpleUnit unit, Point end){
		unitsToRoute.add(unit);
		destinations.add(end);
	}
	
	/**
	 * Update the path handler to route next overflow route
	 */
	public void update(){
		if(!this.isEmpty() && !unitsToRoute.isEmpty()){
			SimpleUnit unit = unitsToRoute.poll();
			Point end = destinations.poll();
			unit.setDestination(end);
		}
	}
	
	
	
}
