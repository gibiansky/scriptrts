package com.scriptrts.ai;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Separation {
	public static void update(AgentGroup group, Agent agent){
		ArrayList<Agent> agents = new ArrayList<Agent>(group.getAgents());
		agents.add(group.getLeader());
		for(Agent a : agents){
			if(!agent.equals(a)){
				Point2D.Double f = new Point2D.Double(agent.x - a.x, agent.y - a.y);
				double r = f.distance(0, 0);
				agent.ax += f.x / Math.pow(r, 2);
				agent.ay += f.y / Math.pow(r, 2);
			}
		}
	}
}
