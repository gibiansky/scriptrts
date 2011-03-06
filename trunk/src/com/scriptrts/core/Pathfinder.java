package com.scriptrts.core;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import com.scriptrts.game.Direction;
import com.scriptrts.game.SimpleUnit;
import com.scriptrts.game.UnitGrid;

public class Pathfinder {

	/**
	 * Unit which the pathfinder is for - unused
	 */
	private SimpleUnit unit;
	
	/**
	 * Current map instance
	 */
	private Map map;
	
	/**
	 * Terrain at each point on map
	 */
	private TerrainType[][] terrainMap;
	
	/**
	 * Unit grid (for collisions later?) - unused
	 */
	private UnitGrid unitGrid;
	
	/**
	 * Stores terrain costs
	 */
	private HashMap<TerrainType, Integer> terrainValues;
	
	/**
	 * Open list of ids to be checked
	 */
	private int[] heap;
	
	/**
	 * Path lengths corresponding to a given id
	 */
	private int[] pathLengths;
	
	/**
	 * Coordinates of the point corresponding to a given id
	 */
	private int[][] coords;
	
	/**
	 * Whether a point is 1: on the open list, 0: unchecked, -1 checked 
	 */
	private int[][] pointChecked;
	
	/**
	 * Parent id of a point (x,y)
	 */
	private int[][] parent;
	
	/**
	 * Unique id for each point added to open list
	 */
	private int nodeID;
	
	/**
	 * Number of points in the open list
	 */
	private int count;
	
	/**
	 * Size of unit grid
	 */
	private int n;
	
	/**
	 * List of Points in path
	 */
	private ArrayList<Point> path;
	
	/**
	 * List of Directions in path
	 */
	private ArrayList<Direction> directions;

	/**
	 * Create a new Pathfinder
	 * @param u unit which pathfinder is for
	 * @param m current map instance
	 * @param g unit grid
	 */
	public Pathfinder(SimpleUnit u, Map m, UnitGrid g){
		unit = u;
		map = m;
		terrainMap = map.getTileArray();
		unitGrid = g;
		n = map.getN() * UnitGrid.SPACES_PER_TILE;
		heap = new int[n * n];
		pathLengths = new int[n * n];
		coords = new int[n * n][2];
		pointChecked = new int[n][n];
		parent = new int[n][n];
		path = new ArrayList<Point>();
		directions = new ArrayList<Direction>();
		setTerrainValues();
	}

	/**
	 * Set the terrain costs for each type of terrain
	 */
	public void setTerrainValues(){
		terrainValues = new HashMap<TerrainType, Integer>();
		terrainValues.put(TerrainType.Grass, 1);
		terrainValues.put(TerrainType.Dirt, 1);
		terrainValues.put(TerrainType.Sand, 2);
		terrainValues.put(TerrainType.Rock, 1);
		terrainValues.put(TerrainType.Water, 500);
		terrainValues.put(TerrainType.DeepFire, 3);
	}

	/**
	 * Calculates the route between two points
	 */
	public void findRoute(int startX, int startY, int endX, int endY){

		/* Add the starting point to the open point list */
		pointChecked[startX][startY] = 1;
		coords[nodeID][0] = startX;
		coords[nodeID][1] = startY;
		parent[startX][startY] = -1;
		add();
		
		/* While the end point has not been added to the closed list */
		while(pointChecked[endX][endY] != -1){
			
			/* Find the point with the shortest path length */
			int shortestPath = remove();
			int nextX = coords[shortestPath][0];
			int nextY = coords[shortestPath][1];
			pointChecked[nextX][nextY] = -1;

			/* Length of path from start point to current point */
			int currentLength = pathLengths[shortestPath];

			/* Find the neighbors of the current point */
			int[][] neighbors = unitGrid.getNeighbors(nextX, nextY);
			
			for(int i = 0; i < neighbors.length; i++){
				int x = neighbors[i][0];
				int y = neighbors[i][1];
				
				/* Only check neighbors not on the closed list */
				if(pointChecked[x][y] != -1){
					
					/* Map tile corresponding to unit grid tile */
					int[] mapTile = unitGrid.getMapTile(x, y);
					
					/* Increment path length by length of path from current point to neighbor point */
					double dlength = dist2D(nextX, nextY, x, y) * terrainValues.get(terrainMap[mapTile[0]][mapTile[1]]) + manhattan(x, y, endX, endY);
					int newLength = currentLength + (int)dlength;

					/* If neighbor is not on open list, add to open list and update info*/
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
						
						/* If better path exists, update info */
						if(newLength < oldLength){
							pathLengths[heap[loc]] = newLength;
							parent[x][y] = shortestPath;
							heapUp(loc);
						}
					}
				}
			}
		}

		/* Retrace path starting from endpoint */
		retrace(endX, endY);
		
		/* Update directions along path*/
		getDirections();
	}

	/**
	 * Distance between two points
	 */
	private double dist2D(int startX, int startY, int endX, int endY){
		return Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
	}

	/**
	 * Manhattan / taxicab distance between two points
	 */
	private int manhattan(int startX, int startY, int endX, int endY){
		return Math.abs(endX - startX) + Math.abs(endY - startY);
	}

	/**
	 * Adds element to heap
	 */
	private void add(){
		/* Add current id to end of heap */
		heap[count] = nodeID;

		/* Heap up */
		heapUp(count);

		/* Increment id and number of open points in heap */
		nodeID++;
		count++;
	}

	/**
	 * Removes first element from heap
	 */
	private int remove(){
		/* Move last element to top of heap */
		int id = heap[0];
		heap[0] = heap[count - 1];
		
		/* Decrement number of open points in heap */
		count--;

		/* Heap down */
		heapDown();

		return id;
	}

	/**
	 * Finds the id in the heap corresponding to the point (x,y)
	 * @return -1 if not found
	 */
	private int find(int x, int y){
		int loc = -1;
		for(int i = 0; i < count; i++)
			if(coords[heap[i]][0] == x && coords[heap[i]][1] == y){
				loc = i;
				break;
			}
		return loc;
	}

	/**
	 * Heap up
	 * @param start starting position in heap
	 */
	private void heapUp(int start){
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

	/**
	 * Heap down
	 */
	private void heapDown(){
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

	/**
	 * Retrace the path starting at the end point (x,y)
	 */
	private void retrace(int x, int y){
		path.add(0, new Point(x, y));
		if(parent[x][y] != -1){
			int parentID = parent[x][y];
			retrace(coords[parentID][0], coords[parentID][1]);
		}
	}

	/**
	 * Get the path found
	 * @return path
	 */
	public ArrayList<Point> getPath(){
		return path;
	}

	/**
	 * Get directions corresponding to the path found
	 * @return directions
	 */
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

	/**
	 * Get direction of p2 relative to p1
	 */
	private Direction getDirection2Pts(Point p1, Point p2){
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
}