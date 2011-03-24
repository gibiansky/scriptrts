package com.scriptrts.game.path;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import com.scriptrts.game.Direction;
import com.scriptrts.game.GameObject;
import com.scriptrts.game.Map;
import com.scriptrts.game.MapGrid;
import com.scriptrts.game.TerrainType;
import com.scriptrts.game.UnitClass;


public class Pathfinder extends Thread{
	/**
	 * Unit to find path for
	 */
	private GameObject unit;

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
	private MapGrid mapGrid;

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
	private int n;

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
	 * Create a new Pathfinder
	 * @param m current map instance
	 * @param g unit grid
	 */
	public Pathfinder(Map m, MapGrid g){
		map = m;
		terrainMap = map.getTileArray();
		mapGrid = g;
		n = map.getN() * MapGrid.SPACES_PER_TILE;
		heap = new Node[n * n];
		nodeList = new HashMap<Point, Node>();
		path = new ArrayList<Point>();
		directions = new LinkedList<Direction>();
		setTerrainValues();
	}

	/**
	 * Create a new Pathfinder
	 * @param u unit which pathfinder is for
	 * @param m current map instance
	 * @param g unit grid
	 */
	public Pathfinder(GameObject u, Map m, MapGrid g){
		this(m, g);
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
		terrainValues.put(TerrainType.Sand, 2);
		terrainValues.put(TerrainType.Rock, 1);
		terrainValues.put(TerrainType.Water, 500);
		terrainValues.put(TerrainType.DeepFire, 3);
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

		/* While the end point has not been added to the closed list */
		while(!nodeList.containsKey(end)){

			/* Find the point with the shortest path length */
			Node next = remove();

			/* Add it to the closed list */
			next.setClosed();
			nodeList.put(next.getPoint(), next);

			/* Length of path from start point to current point */
			int currentLength = next.getMinPathLength();

			/* Find the neighbors of the current point */
			Point[] neighbors = mapGrid.getNeighbors(next.getX(), next.getY());

			for(int i = 0; i < neighbors.length; i++){
				Point p = neighbors[i];

				if(mapGrid.getUnit(p.x, p.y) != null && !(mapGrid.getUnit(p.x, p.y).getUnit().getUnitClass() == UnitClass.Standard)){
					System.out.println(p);
					Node n = new Node(p.x, p.y);
					n.setClosed();
					nodeList.put(p, n);
				}

				/* Only check neighbors not on the closed list */
				if(!nodeList.containsKey(p) || nodeList.get(p).isOpen()){

					/* Map tile corresponding to unit grid tile */
					int[] mapTile = mapGrid.getMapTile(p.x, p.y);

					/* Increment path length by length of path from current point to neighbor point */
					double dlength = dist2D(next.getX(), next.getY(), p.x, p.y) * terrainValues.get(terrainMap[mapTile[0]][mapTile[1]]) + manhattan(p.x, p.y, endX, endY);
					int newLength = currentLength + (int)dlength;

					/* If neighbor is not on open list, add to open list and update info */
					if(!nodeList.containsKey(p)){
						Node newNode = new Node(p.x, p.y);
						newNode.setMinPathLength(newLength);
						newNode.setOpen();
						newNode.setParent(next);
						nodeList.put(p, newNode);
						add(newNode);
					}

					/* Otherwise neighbor is on open list, so check if better path exists */
					else{
						/* Location in heap */
						int loc = find(p.x,p.y);
						Node oldNode = heap[loc];
						int oldLength = oldNode.getMinPathLength();

						/* If better path exists, update info */
						if(newLength < oldLength){
							oldNode.setMinPathLength(newLength);
							oldNode.setParent(next);
							heapUp(loc);
						}
					}
				}
			}
		}

		/* Retrace path starting from endpoint */
		retrace(nodeList.get(end));
		System.out.println(path);
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
		heap[0] = heap[count - 1];

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
			if(heap[i].getMinPathLength() <= heap[(i-1)/2].getMinPathLength()){
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
				if(heap[parent].getMinPathLength() > heap[2*child + 1].getMinPathLength())
					parent = 2*child + 1;
				if(heap[parent].getMinPathLength() > heap[2*child + 2].getMinPathLength())
					parent = 2*child + 2;
			} else if(2*parent + 1 <= count){
				if(heap[parent].getMinPathLength() > heap[2*child + 1].getMinPathLength())
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
	 e @return directions
	 */
	public Queue<Direction> getDirections(){
		if(path.size() == 0)
			return directions;
		Iterator<Point> itr = path.iterator();
		Point current = (Point) itr.next();
		Point next = (Point) itr.next();
		while(itr.hasNext()){
			directions.add(getDirection2Pts(current, next));
			current = next;
			next = (Point) itr.next();
		}
		directions.add(getDirection2Pts(current, next));
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

	public void run(){
		findRoute(unit, endX, endY);
		unit.setPath(getDirections());
		this.reset();
		pathHandler.add(this);
	}
}
