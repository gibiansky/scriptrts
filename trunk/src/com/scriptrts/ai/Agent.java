package com.scriptrts.ai;

public class Agent {

	public double x, y, vx, vy, ax, ay;

	public Agent(double ix, double iy) {
		x = ix;
		y = iy;
	}

	public void update(double dt){
		vx += ax * dt;
		vy += ay * dt;
		x += vx * dt;
		y += vy * dt;
		ax = 0;
		ay = 0;
	}
}
