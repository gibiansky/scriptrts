package com.scriptrts.command;

import com.scriptrts.core.UnitLocation;

public class MovementCommand {

	private int dx, dy;
	private UnitLocation dLoc;
	
	public MovementCommand(int dx, int dy) {
		this.dx = dx;
		this.dy = dy;
	}
	
	public int getX() {
		return dx;
	}
	
	public int getY() {
		return dy;
	}
}
