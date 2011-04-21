package com.scriptrts.game;

import java.awt.Color;
import java.io.IOException;

import com.scriptrts.core.Main;
import com.scriptrts.util.ResourceManager;


/**
 * Class representing the terrain tiles of a map.
 */
public class GameMap {

	/**
	 * Length of the map along one edge
	 */
	private int origN;

	/**
	 * Length of the map used for calculation purposes
	 * If origN is not of the form 2^k+1, this is the next highest number of that form
	 */
	private int n;

	/**
	 * Heights of locations on the map.
	 */
	private double[][] heightArray; 

	/**
	 * Type of terrain at each point on the map.
	 */
	private TerrainType[][] tileArray;


	/**
	 * Side length of smallest square / diagonal of diamond
	 */
	private int length;

	/**
	 * Random number generator used to generate random map.
	 */
	private java.util.Random random = new java.util.Random();

	/**
	 * Player used to place terrain objects
	 */
	private Player terrainPlayer;

	/**
	 * Create a new map.
	 * @param n size of the map along one edge.
	 */
	public GameMap(int n) {
		this.origN = n;
		this.n = (int) (Math.pow(2, Math.ceil(Math.log(n - 1) / Math.log(2))) + 1);
		heightArray = new double[this.n][this.n];
		tileArray = new TerrainType[origN][origN];
		terrainPlayer = new Player("", new Color(255, 255, 255, 1), -1);
	}

	/**
	 * Get the map heightmap
	 * @return the height
	 */
	public double[][] getHeightArray(){
		return heightArray;
	}

	/**
	 * Get the terrain of the map
	 * @return the terrain
	 */
	public TerrainType[][] getTileArray() {
		return tileArray;
	}


	/**
	 * Get the size of the map
	 * @return the size of the map along one edge
	 */
	public int getN() {
		return origN;
	}	

	/**
	 * Generate random map using diamond-square algorithm described
	 * at http://www.gameprogrammer.com/fractal.html.  Each point
	 * on the map is given a corresponding height value.  Noise changes
	 * the "jaggedness" of final surface (0 &lt; noise &lt; 1, 1 is most)
	 * @param noise how much noise to include on the map
	 * @param smoothNum number of times to smooth map after it has been created (0 is regular)
	 */
	public void generateMap(double noise, int smoothNum){
		/* Set initial height at corners */
		int[] corners = {
				0, n-1
		};
		for(int i : corners)
			for(int j : corners)
				heightArray[i][j] = 2 * random.nextDouble() - 1;
		length = n-1;
		int origLength = length;

		/* Loop through and calculate the height at each point recursively.
		 * Sizes of squares and diamonds get progressively smaller. */
		while(length > 1){				
			/* Calculate the maximum allowed change in height within
			 *  1 iteration (depends on noise) */
			double d = Math.pow(noise, Math.log(origLength/length)/Math.log(2) + 1);
			/* Square step, for each square of 4 points, the middle point's
			 * height is the average of the 4, plus a random displacement */
			for(int i = 0; i < n - length; i += length){
				for(int j = 0; j < n - length; j += length){
					//middle of square is the average of the four corners
					heightArray[i + length/2][j + length/2] = d * (2 * random.nextDouble() - 1) + (heightArray[i][j] + heightArray[i][j + length] + heightArray[i + length][j] + heightArray[i + length][j + length])/4;
				}
			}

			/* Diamond step, for each diamond of 4 points, the middle point's
			 * height is the average of the 4, plus a random displacement */
			/* First loop through "diamonds" on the edge of the map that
			 * contain only 3 points */
			for(int i = 0; i < n - length; i += length){
				/* Top edge */
				heightArray[i + length/2][0] = d * (2 * random.nextDouble() - 1) + (heightArray[i][0] + heightArray[i + length][0] + heightArray[i + length/2][length/2])/3;
				/* Bottom edge */
				heightArray[i + length/2][n-1] = d * (2 * random.nextDouble() - 1) + (heightArray[i][n-1] + heightArray[i + length][n-1] + heightArray[i + length/2][n-1 - length/2])/3;
				/* Left edge */
				heightArray[0][i + length/2] = d * (2 * random.nextDouble() - 1) + (heightArray[0][i] + heightArray[0][i + length] + heightArray[length/2][i + length/2])/3;
				/* Right edge */
				heightArray[n-1][i + length/2] = d * (2 * random.nextDouble() - 1) + (heightArray[n-1][i] + heightArray[n-1][i + length] + heightArray[n-1 - length/2][i + length/2])/3;
			}

			/* Then loop through the rest of the diamonds */
			for(int i = length/2; i < n - length/2; i += length/2){
				for(int j = i % length; j < n - length; j += length){
					heightArray[i][j + length/2] = d * (2 * random.nextDouble() - 1) + (heightArray[i][j] + heightArray[i - length/2][j + length/2] + heightArray[i + length/2][j + length/2] + heightArray[i][j + length])/4;
				}
			}
			length /= 2;
		}
		populateTiles();
		smoothOut(smoothNum);
		addTerrain();
	}

	/**
	 * Randomly populate terrain tiles
	 */
	public void populateTiles(){
		/* The height values generated mostly range from -1 to 1
		 * (sort of binomially distributed?)  Play around with
		 * the ranges here...
		 */
		TerrainType[] terrains = TerrainType.values();
		for(int i = 0; i < origN; i++)
			for(int j = 0; j < origN; j++){
				double val = heightArray[i][j];
				if(-20 <= val && val < -.5)
					tileArray[i][j] = terrains[5];
				else if(-.5 <= val && val < -.2)
					tileArray[i][j] = terrains[1];
				else if(-.2 <= val && val < 0)
					tileArray[i][j] = terrains[3];
				else if(0 <= val && val < .2)
					tileArray[i][j] = terrains[4];
				else if(.2 <= val && val < .5)
					tileArray[i][j] = terrains[0];
				else if(.5 <= val && val < 20)
					tileArray[i][j] = terrains[2];
			}
	}

	/**
	 * Smoothes out the map by looping through each tile and if 3 or more of its N/E/S/W neighbors
	 * have the same terrain type, sets its own terrain type to match theirs
	 * @param numTimes number of times to iterate through
	 */
	public void smoothOut(int numTimes){
		for(int count = 0; count < numTimes; count++){
			TerrainType t1,t2,t3,t4;
			for(int i = 1; i < origN-1; i++)
				for(int j = 1; j < origN-1; j++){
					t1 = tileArray[i-1][j];
					t2 = tileArray[i+1][j];
					t3 = tileArray[i][j-1];
					t4 = tileArray[i][j+1];
					TerrainType terrain = findMaxTerrain(t1,t2,t3,t4);
					if(terrain != null)
						tileArray[i][j] = terrain;
				}
		}
	}

	/**
	 * Returns the terrain type that occurs most frequently out of the given terrains
	 * If tie, returns null
	 * @return terrain type that occurs most frequently
	 */

	public TerrainType findMaxTerrain(TerrainType t1, TerrainType t2, TerrainType t3, TerrainType t4){
		/* Arrays to work with */
		TerrainType[] input = {t1, t2, t3, t4};
		int[] counts = {1,1,1,1};

		/* Loop through and count how many matches each terrain type has */
		for(int i = 0; i < input.length - 1; i++)
			for(int j = i+1; j < input.length; j++)
				if(input[i] == input[j]){
					counts[i]++;
					counts[j]++;
				}

		/* Find the maximum number of matches */
		int max = 0;
		int indexOfMax = 0;
		for(int i = 0; i < counts.length; i++){
			if(counts[i] > max){
				max = counts[i];
				indexOfMax = i;
			}
		}

		/* Maximum of 2 matches means it was a tie, so return null */
		if(max == 2)
			return null;

		/* Otherwise return the terrain found most often */
		else
			return input[indexOfMax];
	}

	/**
	 * Add terrain objects, for now just volcanos
	 */
	public void addTerrain(){
		/* Terrain type at each map tile */
		TerrainType[] terrains = TerrainType.values();

			/* Loop through each square and add volcanos randomly */
			for(int i = 5; i < origN - 5; i++)
				for(int j = 5; j < origN - 5; j++)
					if(tileArray[i][j] == terrains[5])
						if(random.nextDouble() < 0.005){
							addVolcano(MapGrid.SPACES_PER_TILE * i + MapGrid.SPACES_PER_TILE / 2, 
									MapGrid.SPACES_PER_TILE * j + MapGrid.SPACES_PER_TILE / 2);
						}
	}

	public void addVolcano(int i, int j){
		try{
			Sprite[] sprites = ResourceManager.loadSpriteSet("volcano.sprite", null);
			GameObject volcano = new GameObject(terrainPlayer, sprites, null, 0, i, j, Direction.North, UnitShape.SHAPE_VOLCANO, UnitClass.Terrain);
			Main.getGame().grid.placeUnit(volcano);
			//Main.getGame().gameManager.addUnit(volcano);
		} catch(IOException e){
			e.printStackTrace();
		}
	}
}
