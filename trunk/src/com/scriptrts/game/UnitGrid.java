package com.scriptrts.game;

import com.scriptrts.combat.Unit;

public class UnitGrid {

	private int n;
	private Unit[][] unitGrid;
	
	public UnitGrid(int n) {
		this.n = n;
		unitGrid = new Unit[n][n];
	}
	
}
