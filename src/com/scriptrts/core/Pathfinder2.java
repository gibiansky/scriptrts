package com.scriptrts.core;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import com.scriptrts.game.Node;
import com.scriptrts.game.SimpleUnit;
import com.scriptrts.game.UnitGrid;

public class Pathfinder2 {
	
	private SimpleUnit unit;
	private Map map;
	private TerrainType[][] terrainMap;
	private UnitGrid unitGrid;
	private Point start, end;
	private HashMap<TerrainType, Integer> terrainValues;
	private boolean[][] checked;
	private HashMap<Point, Integer> minPathLengths;
	private ArrayList<Node> nodesToCheck;
	private boolean found;
	private ArrayList<Point> path;
	
	public Pathfinder2(SimpleUnit u, Map m, UnitGrid g){
		unit = u;
		map = m;
		terrainMap = m.getTileArray();
		unitGrid = g;
		checked = new boolean[m.getN()][m.getN()];
		minPathLengths = new HashMap<Point, Integer>();
		nodesToCheck = new ArrayList<Node>();
		found = false;
		path = new ArrayList<Point>();
		setTerrainValues();
	}
	
	public void setStart(Point p){
		start = p;
	}
	
	public void setEnd(Point p){
		end = p;
	}
	
	public void setTerrainValues(){
		terrainValues = new HashMap<TerrainType, Integer>();
		terrainValues.put(TerrainType.Grass, 1);
		terrainValues.put(TerrainType.Dirt, 1);
		terrainValues.put(TerrainType.Sand, 2);
		terrainValues.put(TerrainType.Rock, 1);
		terrainValues.put(TerrainType.Water, 5);
		terrainValues.put(TerrainType.DeepFire, 3);
	}
	
	public void findRoute(Node start, Node end){
		if(start.equals(end)){
			System.out.println("Done.");
			found = true;
			//System.out.println(retrace(start).toString());
			return;
		}
		Node[] neighbors = map.getNeighbors(start);
		//System.out.println("Checking " + start.toString());
		for(Node n : neighbors){
			Point p = n.getPoint();
			if(!checked[p.x][p.y]){
				int dlength = (int)(10 * dist2D(start.getPoint(), p)) *	terrainValues.get(terrainMap[p.x][p.y]) + 10 * manhattan(p, end.getPoint());
				int currentLength = start.getPathLength() + dlength;
				if(!minPathLengths.containsKey(p) || currentLength < minPathLengths.get(p)){
					if(minPathLengths.containsKey(p)){
						remove(n);
					}
					n.setPathLength(currentLength);
					n.setParent(start);
					minPathLengths.put(p, currentLength);
					nodesToCheck.add(n);
				}
			}
		}
		checked[start.getPoint().x][start.getPoint().y] = true;
		//System.out.println(nodesToCheck.toString());
		if(nodesToCheck.isEmpty()){
			System.out.println("No path found.");
			return;
		}
		if(!found)
			findRoute(nodesToCheck.remove(0), end);
	}
	
	public void retrace(Node n){
		path.add(0, n.getPoint());
		if(n.hasParent()){
			System.out.println(n.toString());
			retrace(n.getParent());
		}
	}
	
	public ArrayList<Point> getPath(){
		return path;
	}
	
	public double dist2D(Point start, Point end){
		return Math.sqrt(Math.pow(end.x - start.x, 2) + Math.pow(end.y - start.y, 2));
	}
	
	public int manhattan(Point start, Point end){
		return Math.abs(end.x - start.x) + Math.abs(end.y - start.y);
	}
	
	public void add(Node n){
		for(int i = 0; i < nodesToCheck.size(); i++)
			if(nodesToCheck.get(i).getPathLength() > n.getPathLength())
				nodesToCheck.add(i, n);
	}
	
	public void remove(Node n){
		for(int i = 0; i < nodesToCheck.size(); i++){
			if(nodesToCheck.get(i).equals(n))
				nodesToCheck.remove(i);
		}
	}
}
