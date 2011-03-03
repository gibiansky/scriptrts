package com.scriptrts.core;

/**
 * Class representing the terrain tiles of a map.
 */
public class Map {

    /**
     * Length of the map along one edge
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
     * ????????????????????????
     */
	private int length;

    /**
     * Random number generator used to generate random map.
     */
    private java.util.Random random = new java.util.Random();

    /**
     * Create a new map.
     * @param n size of the map along one edge.
     */
	public Map(int n) {
		this.n = n;
		heightArray = new double[n][n];
		tileArray = new TerrainType[n][n];
	}

	/**
	 * @return the height
	 */
	public double[][] getHeightArray(){
		return heightArray;
	}
	
	/**
	 * @return the terrain
	 */
	public TerrainType[][] getTileArray() {
		return tileArray;
	}

	/**
	 * @return the size of the map along one edge
	 */
	public int getN() {
		return n;
	}
	
	/**
	 * Generate random map
     * @param noise how much noise to include on the map
	 */
	public void generateMap(double noise){
		//Set initial heights at corners
		int[] corners = {0, n-1};
		for(int i : corners)
			for(int j : corners)
				heightArray[i][j] = 2 * random.nextDouble() - 1;
		length = n-1;
		int origLength = length;
			
		//Loop through and calculate height recursively
		while(length > 1){				
			//calculate the maximum displacement due to noise
			double d = Math.pow(noise, Math.log(origLength/length)/Math.log(2) + 1);
			//square step
			for(int i = 0; i < n - length; i += length){
				for(int j = 0; j < n - length; j += length){
					//middle of square is the average of the four corners
					heightArray[i + length/2][j + length/2] = d * (2 * random.nextDouble() - 1) + (heightArray[i][j] + heightArray[i][j + length] + heightArray[i + length][j] + heightArray[i + length][j + length])/4;
				}
			}
			
			//diamond step
			//first loop through edges
			for(int i = 0; i < n - length; i += length){
				//top edge
				heightArray[i + length/2][0] = d * (2 * random.nextDouble() - 1) + (heightArray[i][0] + heightArray[i + length][0] + heightArray[i + length/2][length/2])/3;
				//bottom edge
				heightArray[i + length/2][n-1] = d * (2 * random.nextDouble() - 1) + (heightArray[i][n-1] + heightArray[i + length][n-1] + heightArray[i + length/2][n-1 - length/2])/3;
				//left edge
				heightArray[0][i + length/2] = d * (2 * random.nextDouble() - 1) + (heightArray[0][i] + heightArray[0][i + length] + heightArray[length/2][i + length/2])/3;
				//right edge
				heightArray[n-1][i + length/2] = d * (2 * random.nextDouble() - 1) + (heightArray[n-1][i] + heightArray[n-1][i + length] + heightArray[n-1 - length/2][i + length/2])/3;
			}
			
			//then loop through middle points
			for(int i = length/2; i < n - length/2; i += length/2){
				for(int j = i % length; j < n - length; j += length){
					heightArray[i][j + length/2] = d * (2 * random.nextDouble() - 1) + (heightArray[i][j] + heightArray[i - length/2][j + length/2] + heightArray[i + length/2][j + length/2] + heightArray[i][j + length])/4;
				}
			}
			length /= 2;
		}
		populateTiles();
		
	}
	
	/**
	 * Randomly populate terrain tiles
	 */
	public void populateTiles(){
		TerrainType[] terrains = TerrainType.values();
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				double val = heightArray[i][j];
				if(-20 <= val && val < -.5)
					tileArray[i][j] = terrains[0];
				else if(-.5 <= val && val < -.2)
					tileArray[i][j] = terrains[1];
				else if(-.2 <= val && val < 0)
					tileArray[i][j] = terrains[3];
				else if(0 <= val && val < .2)
					tileArray[i][j] = terrains[5];
				else if(.2 <= val && val < .5)
					tileArray[i][j] = terrains[2];
				else if(.5 <= val && val < 20)
					tileArray[i][j] = terrains[4];
			}
		}
	}
}
