package com.scriptrts.game;


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
     * Side length of smallest square / diagonal of diamond
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
		return n;
	}	
	
	/**
	 * Generate random map using diamond-square algorithm described
	 * at http://www.gameprogrammer.com/fractal.html.  Each point
	 * on the map is given a corresponding height value.  Noise changes
	 * the "jaggedness" of final surface (0 &lt; noise &lt; 1, 1 is most)
     * @param noise how much noise to include on the map
	 */
	public void generateMap(double noise){
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
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
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
	}
}
