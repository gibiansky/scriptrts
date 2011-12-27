package com.scriptrts.ai;

import java.awt.geom.Point2D;

public class Arrival {
	
	static double slowingRadius = 1;
	
	public static void update(AgentGroup group, Agent agent){
		Agent leader = group.getLeader();
		Point2D.Double offset = new Point2D.Double(leader.x - agent.x, leader.y - agent.y);
		double r = offset.distance(0, 0);
		double vScale = Math.min(r / slowingRadius, AITest.V_MAX);
		Point2D.Double v = new Point2D.Double(offset.x * vScale / r, offset.y * vScale / r);
		Point2D.Double f = new Point2D.Double(v.x - agent.vx, v.y - agent.vy);
		agent.ax += f.x;
		agent.ay += f.y;
	}
}
