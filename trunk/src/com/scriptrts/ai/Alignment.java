package com.scriptrts.ai;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Alignment {
	public static void update(AgentGroup group, Agent agent){
		ArrayList<Agent> agents = new ArrayList<Agent>(group.getAgents());
		agents.add(group.getLeader());
		double avgVx = 0, avgVy = 0;
		for(Agent a : agents){
			avgVx += a.vx;
			avgVy += a.vy;
		}
		avgVx /= agents.size();
		avgVy /= agents.size();
		Point2D.Double f = new Point2D.Double(avgVx - agent.vx, avgVy - agent.vy);
		agent.ax += f.x;
		agent.ay += f.y;
	}
}
