package com.scriptrts.game;


public class UnitGrid {

	private int n;
	public UnitTile[][] unitGrid;
	
	public UnitGrid(int n) {
		this.n = n;
		unitGrid = new UnitTile[n][n];
	}

}
