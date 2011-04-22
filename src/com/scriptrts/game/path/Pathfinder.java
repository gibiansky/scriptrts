package com.scriptrts.game.path;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import com.scriptrts.core.Main;
import com.scriptrts.game.Direction;
import com.scriptrts.game.GameObject;
import com.scriptrts.game.GameMap;
import com.scriptrts.game.MapGrid;
import com.scriptrts.game.TerrainType;


public class Pathfinder extends Thread{
	/**
	 * Unit to find path for
	 */
	private GameObject unit;

	/**
	 * Current map instance
	 */
	private GameMap map = Main.getGame().getCurrentMap();

	/**
	 * Terrain at each point on map
	 */
	private TerrainType[][] terrainMap = map.getTileArray();

	/**
	 * Unit grid
	 */
	private MapGrid mapGrid = Main.getGame().getGameGrid();

	/**
	 * Stores terrain costs
	 */
	private HashMap<TerrainType, Integer> terrainValues;

	/**
	 * Open list of nodes to be checked
	 */
	private Node[] heap;

	/**
	 * List of nodes corresponding to each unit tile on the map
	 */
	private HashMap<Point, Node> nodeList;

	/**
	 * Number of points in the open list
	 */
	private int count;

	/**
	 * Size of unit grid
	 */
	private int n = map.getN() * MapGrid.SPACES_PER_TILE;

	/**
	 * List of Points in path
	 */
	private ArrayList<Point> path;

	/**
	 * List of Directions in path
	 */
	private Queue<Direction> directions;

	/**
	 * End coordinates of path
	 */
	private int endX, endY;

	/**
	 * Path handler queue used to manage this pathfinder
	 */
	private PathHandler pathHandler;

	
	/**
	 * Number of nodes to search before giving up
	 */
	private int threshold = n * n / 10;
	
	/**
	 * Create a new Pathfinder
	 */
	public Pathfinder(){
		heap = new Node[n * n];
		nodeList = new HashMap<Point, Node>();
		path = new ArrayList<Point>();
		directions = new LinkedList<Direction>();
		setTerrainValues();
	}

	/**
	 * Create a new Pathfinder
	 * @param u unit which pathfinder is for
	 */
	public Pathfinder(GameObject u){
		this();
		unit = u;
	}

	/**
	 * Reset and clear path when done
	 */
	public void reset(){
		for(int i = 0; i < count; i++){
			heap[i].reset();
		}
		nodeList.clear();
		path.clear();
		directions = new LinkedList<Direction>();
		count = 0;
	}

	/**
	 * Set the terrain costs for each type of terrain
	 */
	public void setTerrainValues(){
		terrainValues = new HashMap<TerrainType, Integer>();
		terrainValues.put(TerrainType.Grass, 1);
		terrainValues.put(TerrainType.Dirt, 1);
		terrainValues.put(TerrainType.Sand, 1);
		terrainValues.put(TerrainType.Rock, 1);
		terrainValues.put(TerrainType.Water, 1);
		terrainValues.put(TerrainType.DeepFire, 1);
	}

	/**
	 * Set the unit to route
	 */
	public void setUnit(GameObject unit){
		this.unit = unit;
	}

	/**
	 * Set the destination
	 */
	public void setDestination(int endX, int endY){
		this.endX = endX;
		this.endY = endY;
	}

	/**
	 * Set the path handler
	 */
	public void setPathHandler(PathHandler pathHandler){
		this.pathHandler = pathHandler;
	}

	/**
	 * Calculates the route between two points
	 */
	public void findRoute(GameObject u, int endX, int endY){

		Node start = new Node(u.getUnit().getX(), u.getUnit().getY());
		Point end = new Point(endX, endY);

		if(start.getPoint().equals(end))
			return;

		/* Add the starting point to the open point list */
		start.setOpen();
		start.setParent(null);
		nodeList.put(start.getPoint(), start);
		add(start);

		int tilesChecked = 1;
		
		/* While the end point has not been added to the closed list */
		while(!nodeList.containsKey(end) && tilesChecked < threshold){

			/* Find the point with the shortest path length */
			Node next = remove();
			if(next == null)
				break;

			/* Add it to the closed list */
			next.setClosed();
			nodeList.put(next.getPoint(), next);

			/* Length of path from start point to current point */
			int currentGCost = next.getGCost();

			/* Find the neighbors of the current point */
			Point[] neighbors = mapGrid.getNeighbors(next.getX(), next.getY());

			for(int i = 0; i < neighbors.length; i++){
				Point p = neighbors[i];

				/* Only check neighbors not on the closed list */
				if(!nodeList.containsKey(p) || nodeList.get(p).isOpen()){

					/* Only check neighbors which can fit the unit shape */
					Direction dir = mapGrid.getDirection(next.getPoint(), p);				
					if(mapGrid.canPlaceUnit(u, next.getPoint().x, next.getPoint().y, dir) && mapGrid.canPlaceUnit(u, p.x, p.y, dir)){

						/* Map tile corresponding to unit grid tile */
						int[] mapTile = mapGrid.getMapTile(p.x, p.y);

						/* Increment path length by length of path from current point to neighbor point */
						int dlength = 1;//dist2D(next.getX(), next.getY(), p.x, p.y) * terrainValues.get(terrainMap[mapTile[0]][mapTile[1]]);
						int newHCost = manhattan(p.x, p.y, endX, endY);
						int newGCost = currentGCost + dlength;

						/* If neighbor is not on open list, add to open list and update info */
						if(!nodeList.containsKey(p)){
							Node newNode = new Node(p.x, p.y);
							newNode.setGCost(newGCost);
							newNode.setHCost(newHCost);
							newNode.setOpen();
							newNode.setParent(next);
							nodeList.put(p, newNode);
							add(newNode);
							/* Increment number of tiles checked */
							tilesChecked++;
						}

						/* Otherwise neighbor is on open list, so check if better path exists */
						else{
							/* Location in heap */
							int loc = find(p.x,p.y);
							Node oldNode = heap[loc];
							int oldFCost = oldNode.getFCost();

							/* If better path exists, update info */
							if(newGCost + newHCost < oldFCost){
								oldNode.setGCost(newGCost);
								oldNode.setHCost(newHCost);
								oldNode.setParent(next);
								heapUp(loc);
							}
						}
					}
					/* If we can't place the unit at the current tile, add the current tile to the closed list */ 
					else{
						Node n = new Node(p.x, p.y);
						n.setClosed();
						n.setParent(next);
						nodeList.put(p, n);
						/* Increment number of tiles checked */
						tilesChecked++;
					}
				}
			}
		}
		
		/* If we broke out of the loop because we gave up searching, pick the node on the open list with the lowest G cost */
		if(!nodeList.containsKey(end))
			retrace(heap[0]);
		/* Otherwise retrace path starting from endpoint */
		else
			retrace(nodeList.get(end));
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
	private void add(Node n){		
		/* Add current node to end of heap */
		heap[count] = n;

		/* Heap up */
		heapUp(count);

		/* Increment number of open points in heap */
		count++;
	}

	/**
	 * Removes first element from heap
	 */
	private Node remove(){
		/* Move last element to top of heap */
		Node first = heap[0];
		try{
			heap[0] = heap[count - 1];
		}
		/* If there are no nodes on the open list, return null */
		catch(ArrayIndexOutOfBoundsException e){
			return null;
		}

		/* Decrement number of open points in heap */
		count--;

		/* Heap down */
		heapDown();

		return first;
	}

	/**
	 * Finds the node (x,y) in the heap
	 * @return -1 if not found
	 */
	private int find(int x, int y){
		int loc = -1;
		for(int i = 0; i < count; i++)
			if(heap[i].getX() == x && heap[i].getY() == y){
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
			if(heap[i].getFCost() <= heap[(i-1)/2].getFCost()){
				Node temp = heap[(i-1)/2];
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
				if(heap[parent].getFCost() > heap[2*child + 1].getFCost())
					parent = 2*child + 1;
				if(heap[parent].getFCost() > heap[2*child + 2].getFCost())
					parent = 2*child + 2;
			} else if(2*parent + 1 <= count){
				if(heap[parent].getFCost() > heap[2*child + 1].getFCost())
					parent = 2*child + 1;
			}
			if(parent > child){
				Node temp = heap[child];
				heap[child] = heap[parent];
				heap[parent] = temp;
			} else
				break;
		}
	}

	/**
	 * Retrace the path starting at the end node (x,y)
	 */
	private void retrace(Node n){
		path.add(0, n.getPoint());
		if(n.getParent() != null){
			Node parent = n.getParent();
			retrace(parent);
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
	public Queue<Direction> getDirections(){
		if(path.size() == 0)
			return directions;
		Iterator<Point> itr = path.iterator();
		Point current = (Point) itr.next();
		Point next = (Point) itr.next();
		while(itr.hasNext()){
			directions.add(mapGrid.getDirection(current, next));
			current = next;
			next = (Point) itr.next();
		}
		directions.add(mapGrid.getDirection(current, next));
		return directions;
	}



	public void run(){
		findRoute(unit, endX, endY);
		unit.setPath(getDirections());
		this.reset();
		pathHandler.add(this);
	}
}
