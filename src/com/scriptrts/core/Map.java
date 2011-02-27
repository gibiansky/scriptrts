package com.scriptrts.core;

public class Map {

	private int n;
	private double[][] heightArray; 
	private TerrainType[][] tileArray;
	private ResourceDensity density;
	private int length;
    private java.util.Random random = new java.util.Random();

	public Map(int n, ResourceDensity d) {
		this.n = n;
		this.density = d;
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
	 * @return the density
	 */
	public ResourceDensity getDensity() {
		return density;
	}

	/**
	 * @return the n
	 */
	public int getN() {
		return n;
	}
	
	/**
	 * Generate random map
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
		populateTiles3();
		
	}
	
	/**
	 * Randomly populate terrain tiles
	 */
	public void populateTiles() {
		// create a base of grass
		for (int i = 0; i < n; i++)
			for(int j = 0; j < n; j++)
				tileArray[i][j] = TerrainType.Grass;
		double param = .005, radius = 4;
		/* Loop through each tile type */
		for(TerrainType t : TerrainType.values()) {
			for (int i = 0; i < n; i++)
				for(int j = 0; j < n; j++)
					if(random.nextDouble() < param) {
						tileArray[i][j] = t;
						int x = i, y = j;
						for (int k = 0; k < n; k++)
							for(int l = 0; l < n; l++)
								if(Math.pow(x - k, 2) + Math.pow(y - l, 2) / 2 < Math.pow(radius, 2))
									tileArray[l][k] = t;
					}
		}
	}

	public void populateTiles2() {
		for (int i = 0; i < n; i++)
			for(int j = 0; j < n; j++)
				tileArray[i][j] = TerrainType.Grass;
		double probability, radius;
		/* Loop through each tile type */
		for(TerrainType t : TerrainType.values()) {
			probability = 0;
		}
	}
	
	public void populateTiles3(){
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
