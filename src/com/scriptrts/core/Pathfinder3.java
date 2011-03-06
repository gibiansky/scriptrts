package com.scriptrts.core;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import com.scriptrts.game.Direction;
import com.scriptrts.game.SimpleUnit;
import com.scriptrts.game.UnitGrid;

public class Pathfinder3 {

	private SimpleUnit unit;
	private Map map;
	private TerrainType[][] terrainMap;
	private UnitGrid unitGrid;
	private HashMap<TerrainType, Integer> terrainValues;
	private int[] heap;
	private int[] pathLengths;
	private int[][] coords;
	private int[][] pointChecked;
	private int[][] parent;
	private int nodeID;
	private int count;
	private int n;
	private ArrayList<Point> path;
	private ArrayList<Direction> directions;

	public Pathfinder3(SimpleUnit u, Map m, UnitGrid g){
		unit = u;
		map = m;
		terrainMap = m.getTileArray();
		unitGrid = g;
		n = m.getN();
		heap = new int[n * n];
		pathLengths = new int[n * n];
		coords = new int[n * n][2];
		pointChecked = new int[n][n];
		parent = new int[n][n];
		path = new ArrayList<Point>();
		directions = new ArrayList<Direction>();
		setTerrainValues();
	}

	public void setTerrainValues(){
		terrainValues = new HashMap<TerrainType, Integer>();
		terrainValues.put(TerrainType.Grass, 1);
		terrainValues.put(TerrainType.Dirt, 1);
		terrainValues.put(TerrainType.Sand, 2);
		terrainValues.put(TerrainType.Rock, 1);
		terrainValues.put(TerrainType.Water, 500);
		terrainValues.put(TerrainType.DeepFire, 3);
	}

	public void findRoute(int startX, int startY, int endX, int endY){

		/* Add the starting point to the open point list */
		pointChecked[startX][startY] = 1;
		coords[nodeID][0] = startX;
		coords[nodeID][1] = startY;
		parent[startX][startY] = -1;
		add();

		while(pointChecked[endX][endY] != -1){
			int shortestPath = remove();
			int nextX = coords[shortestPath][0];
			int nextY = coords[shortestPath][1];
			pointChecked[nextX][nextY] = -1;
			
			int currentLength = pathLengths[shortestPath];

			int[][] neighbors = map.getNeighbors(nextX, nextY);
			for(int i = 0; i < neighbors.length; i++){
				int x = neighbors[i][0];
				int y = neighbors[i][1];
				if(pointChecked[x][y] != -1){
					double dlength = dist2D(nextX, nextY, x, y) * terrainValues.get(terrainMap[x][y]) + manhattan(x, y, endX, endY);
					int newLength = currentLength + (int)dlength;
					
					/* If neighbor is not on open list, add to open list */
					if(pointChecked[x][y] == 0){
						pointChecked[x][y] = 1;
						pathLengths[nodeID] = newLength;
						coords[nodeID][0] = x;
						coords[nodeID][1] = y;
						parent[x][y] = shortestPath;
						add();
					}
					
					/* Otherwise neighbor is on open list, so check if better path exists */
					else{
						/* Location in heap */
						int loc = find(x,y);
						int oldLength = pathLengths[heap[loc]];
						if(newLength < oldLength){
							pathLengths[heap[loc]] = newLength;
							parent[x][y] = shortestPath;
							heapUp(loc);
						}
					}
				}
			}
		}
		
		retrace(endX, endY);
		getDirections();
	}

	public double dist2D(int startX, int startY, int endX, int endY){
		return Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
	}

	public int manhattan(int startX, int startY, int endX, int endY){
		return Math.abs(endX - startX) + Math.abs(endY - startY);
	}

	/**
	 * Adds element to heap
	 */
	public void add(){
		heap[count] = nodeID;

		/* Heap up */
		heapUp(count);

		nodeID++;
		count++;
	}

	/**
	 * Removes first element from heap
	 */
	public int remove(){
		int id = heap[0];
		heap[0] = heap[count - 1];
		count--;

		/* Heap down */
		heapDown();

		return id;
	}

	/**
	 * Finds the position of the point (x,y) in the heap
	 */
	public int find(int x, int y){
		int loc = -1;
		for(int i = 0; i < count; i++)
			if(coords[heap[i]][0] == x && coords[heap[i]][1] == y){
				loc = i;
				break;
			}
		return loc;
	}

	public int[] find(int x1, int y1, int x2, int y2){
		int[] locs = new int[2];
		Arrays.fill(locs, -1);
		for(int i = 0; i <= count; i++){
			if(coords[heap[i]][0] == x1 && coords[heap[i]][1] == y1){
				locs[0] = i;
			} else if(coords[heap[i]][0] == x2 && coords[heap[i]][1] == y2){
				locs[1] = i;
			}
			if(locs[0] != -1 && locs[1] != -1)
				break;
		}
		return locs;
	}

	public void heapUp(int start){
		/* Heap up */
		int i = start;
		while(i > 0){
			if(pathLengths[heap[i]] <= pathLengths[heap[(i-1)/2]]){
				int temp = heap[(i-1)/2];
				heap[(i-1)/2] = heap[i];
				heap[i] = temp;
				i = (i-1)/2;
			} else
				break;
		}
	}
	
	public void heapDown(){
		/* Heap down */
		int parent = 0;
		while(true){
			int child = parent;
			if(2*parent + 2 <= count){
				if(pathLengths[heap[parent]] > pathLengths[heap[2*child + 1]])
					parent = 2*child + 1;
				if(pathLengths[heap[parent]] > pathLengths[heap[2*child + 2]])
					parent = 2*child + 2;
			} else if(2*parent + 1 <= count){
				if(pathLengths[heap[parent]] > pathLengths[heap[2*child + 1]])
					parent = 2*child + 1;
			}
			if(parent > child){
				int temp = heap[child];
				heap[child] = heap[parent];
				heap[parent] = temp;
			} else
				break;
		}
	}
	
	public void retrace(int x, int y){
		path.add(0, new Point(x, y));
		if(parent[x][y] != -1){
			int parentID = parent[x][y];
			retrace(coords[parentID][0], coords[parentID][1]);
		}
	}
	
	public ArrayList<Point> getPath(){
		return path;
	}
	
	public ArrayList<Direction> getDirections(){
		Iterator<Point> itr = path.iterator();
		Point current = (Point) itr.next();
		Point next = (Point) itr.next();
		while(itr.hasNext()){
			directions.add(getDirection2Pts(current, next));
			current = next;
			next = (Point) itr.next();
		}
		return directions;
	}
	
	public Direction getDirection2Pts(Point p1, Point p2){
		int dx = p2.x - p1.x;
		int dy = p2.y - p1.y;
		
		if(dx == -1)
			switch(dy){
				case -1:
					return Direction.Southwest;
				case 0:
					return Direction.West;
				case 1:
					return Direction.Northwest;
				default:
					return null;
						
			}
		else if(dx == 0)
			switch(dy){
				case -1:
					return Direction.South;
				case 1:
					return Direction.North;
				default:
					return null;
			}
		else if(dx == 1)
			switch(dy){
				case -1:
					return Direction.Southeast;
				case 0:
					return Direction.East;
				case 1:
					return Direction.Northeast;
				default:
					return null;
			}
		else
			return null;
	}
	
	/*public void printArray(int[][] array){
		for(int i = 0; i < array.length; i++)
			System.out.print("[" + array[i][0] + "," + array[i][1] + "]");
		System.out.println();
	}
	
	public String printCoords(int id){
		String s = "[" + coords[id][0] + "," + coords[id][1] + "]";
		return s;
	}
	
	public void printHeap(){
		if(count == 0){
			System.out.println("[]");
			return;
		}
		System.out.print("[");
		for(int i = 0; i < count - 1; i++)
			System.out.print(heap[i] + " ");
		System.out.println(heap[count - 1] + "]");
	}
	
	public void printAllCoords(){
		if(nodeID == 0){
			System.out.println("[]");
			return;
		}
		System.out.print("[");
		for(int i = 0; i < nodeID - 1; i++)
			System.out.print(printCoords(i) + " ");
		System.out.println(printCoords(nodeID - 1) + "]");
	}
	
	public void printPathLengths(){
		if(nodeID == 0){
			System.out.println("[]");
			return;
		}
		System.out.print("[");
		for(int i = 0; i < nodeID - 1; i++)
			System.out.print(pathLengths[i] + " ");
		System.out.println(pathLengths[nodeID - 1] + "]");
	}*/
}
