package com.scriptrts.ai;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class AgentGroup {

	private Agent leader;
	private ArrayList<Agent> agents;
	private Point2D.Double centroid;
	
	public AgentGroup(Agent leader){
		this.leader = leader;
		agents = new ArrayList<Agent>();
		centroid = new Point2D.Double(leader.x, leader.y);
	}
	
	public void add(Agent agent){
		int n = agents.size() + 1;
		centroid.x = (centroid.x * n + agent.x) / (n + 1);
		centroid.y = (centroid.y * n + agent.y) / (n + 1);
		agents.add(agent);
	}
	
	public void remove(Agent agent){
		int n = agents.size() + 1;
		centroid.x = (centroid.x * n - agent.x) / (n - 1);
		centroid.y = (centroid.y * n - agent.y) / (n - 1);
		agents.remove(agent);
	}

	public void updateCentroid(){
		centroid.x = leader.x;
		centroid.y = leader.y;
		for(Agent agent : agents){
			centroid.x += agent.x;
			centroid.y += agent.y;
		}
		centroid.x /= agents.size() + 1;
		centroid.y /= agents.size() + 1;
	}
	
	public Agent getLeader() {
		return leader;
	}

	public void setLeader(Agent leader) {
		this.leader = leader;
	}

	public ArrayList<Agent> getAgents() {
		return agents;
	}

	public void setAgents(ArrayList<Agent> agents) {
		this.agents = agents;
	}

	public Point2D.Double getCentroid() {
		return centroid;
	}	
}
