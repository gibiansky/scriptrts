package com.scriptrts.game;

import java.awt.Point;

public class Node implements Comparable<Node>{

	private Point p;
	private int pathLength;
	private Node parent;

	public Node(Point loc){
		p = loc;
		pathLength = -1;
		parent = null;
	}

	public Node(Point loc, int length){
		p = loc;
		pathLength = length;
		parent = null;
	}

	public Point getPoint(){
		return p;
	}

	public int getPathLength(){
		return pathLength;
	}
	
	public Node getParent(){
		return parent;
	}

	public void setPathLength(int length){
		pathLength = length;
	}

	public void setParent(Node n){
		parent = n;
	}

	public boolean hasParent(){
		return !(parent == null);
	}
	
	public int compareTo(Node n){
			return pathLength - n.getPathLength();
	}

	public boolean equals(Node n){
		return p.equals(n.getPoint());
	}

	public String toString(){
		return p.toString();
	}
}
