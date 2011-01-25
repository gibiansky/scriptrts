package com.scriptrts.core;

public class Map {

	private int n;
	private TerrainType[][] tileArray;
	private ResourceDensity density;

	public Map(int n, ResourceDensity d) {
		this.n = n;
		this.density = d;
		tileArray = new TerrainType[n][n];
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
					if(Math.random() < param) {
						tileArray[i][j] = t;
						int x = i, y = j;
						for (int k = 0; k < n; k++)
							for(int l = 0; l < n; l++)
								if(Math.pow(x - k, 2) + Math.pow(y - l, 2) / 2 < Math.pow(radius, 2))
									tileArray[l][k] = t;
					}
		}
	}
}
