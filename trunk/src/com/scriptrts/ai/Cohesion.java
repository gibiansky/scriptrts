package com.scriptrts.ai;

import java.awt.geom.Point2D;

public class Cohesion {
	public static void update(AgentGroup group, Agent agent){
		Point2D.Double centroid = group.getCentroid();
		Point2D.Double f = new Point2D.Double(centroid.x - agent.x, centroid.y - agent.y);
		double r = f.distance(0, 0);
		agent.ax += f.x / Math.pow(r, 2);
		agent.ay += f.y / Math.pow(r, 2);
	}
}
