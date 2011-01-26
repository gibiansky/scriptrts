package com.scriptrts.core;

import java.util.ArrayList;

import com.scriptrts.combat.Unit;
import com.scriptrts.game.UnitGrid;

public class Game {

	private int n;
	private Map gameMap;
	private UnitGrid unitGrid;
	private ArrayList<Unit> units;
	
	public Game() {
		units = new ArrayList<Unit>();
		gameMap = new Map(n, ResourceDensity.Medium);
		unitGrid = new UnitGrid(n * 3);
	}
	
	public static void main(String... args) {
		
	}
	
}
