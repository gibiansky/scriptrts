package com.scriptrts.game.path;

import java.awt.Point;

public class Node {

	/**
	 * x coordinate of the unit tile corresponding to this node
	 */
	private int x;
	
	/**
	 * y coordinate of the unit tile corresponding to this node
	 */
	private int y;
	
	/**
	 * Minimum path length to reach this node from the starting point
	 */
	private int gCost;
	
	/**
	 * Estimated path length from this node to the ending point
	 */
	private int hCost;
	
	/**
	 * Whether or not the node is on the open list
	 */
	private boolean open;
	
	/**
	 * Parent of this node (used for retracing the path)
	 */
	private Node parent;
	
	/**
	 * Create a new node
	 * @param x x coordinate of the node
	 * @param y y coordinate of the node
	 */
	public Node(int x, int y){
		this.x = x;
		this.y = y;
		gCost = 0;
		hCost = 0;
		open = true;
		parent = null;
	}
	
	/**
	 * Reset the values in this node (to save from creating a new node every single time)
	 */
	public void reset(){
		x = 0;
		y = 0;
		gCost = 0;
		hCost = 0;
		open = true;
		parent = null;
	}
	
	/**
	 * Get the x coordinate of the node
	 * @return the x coordinate of the unit tile corresponding to this node
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Set the x coordinate of the node
	 * @param x x coordinate of the node
	 */
	public void setX(int x) {
		this.x = x;
	}
	
	/**
	 * Get the y coordinate of the node
	 * @return the y coordinate of the unit tile corresponding to this node
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Set the y coordinate of the node
	 * @param y y coordinate of the node
	 */
	public void setY(int y) {
		this.y = y;
	}
	
	/**
	 * Get the point corresponding to this node
	 * @return the unit tile corresponding to this node
	 */
	public Point getPoint(){
		return new Point(x, y);
	}
	
	/**
	 * Get the minimum path length to reach this node from the starting point
	 * @return minimum path length to reach this node
	 */
	public int getGCost(){
		return gCost;
	}
	
	/**
	 * Set the minimum path length
	 * @param gCost minimum path length to reach this node
	 */
	public void setGCost(int gCost){
		this.gCost = gCost;
	}
	
	/**
	 * Get the estimated path length from this node to the ending point
	 * @return estimated path length to the end point
	 */
	public int getHCost(){
		return hCost;
	}
	
	/**
	 * Set the estimated path length
	 * @param hCost estimated path length to the end point
	 */
	public void setHCost(int hCost){
		this.hCost = hCost;
	}
	
	/**
	 * Get the total cost f = g + h
	 * @return fCost total cost
	 */
	public int getFCost(){
		return gCost + hCost;
	}
	
	/**
	 * Whether this node is on the open list (true) or closed list (false)
	 * @return whether the node is open or closed
	 */
	public boolean isOpen(){
		return open;
	}
	
	/**
	 * Put the node on the open list
	 */
	public void setOpen(){
		this.open = true;
	}
	
	/**
	 * Put the node on the closed list
	 */
	public void setClosed(){
		this.open = false;
	}
	
	/**
	 * Get the parent of this node
	 * @return the parent of this node
	 */
	public Node getParent() {
		return parent;
	}
	
	/**
	 * Set the parent of this node
	 * @param parent parent of this node
	 */
	public void setParent(Node parent) {
		this.parent = parent;
	}	
}
