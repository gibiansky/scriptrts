package com.scriptrts.ai;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class AITest extends JFrame implements KeyListener, MouseListener {

	private Agent leader;
	private AgentGroup group;
	private TestPanel panel;

	private static final int ACCELERATION = 10;
	public static final int V_MAX = 50;
	private static final double DT = 0.1;

	public AITest() {
		setSize(800, 600);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		panel = new TestPanel();
		add(panel);

		addKeyListener(this);
		addMouseListener(this);

		leader = new Agent(400, 300);
		group = new AgentGroup(leader);

		new Thread() {
			public void run() {
				while(true) {
					leader.update(DT);
					if(leader.x > 800) {
						leader.x = 800;
						leader.vx = 0;
						leader.ax = 0;
					}
					if(leader.x < 0) {
						leader.x = 0;
						leader.vx = 0;
						leader.ax = 0;
					}
					if(leader.y > 600) {
						leader.y = 600;
						leader.vy = 0;
						leader.ay = 0;
					}
					if(leader.y < 0) {
						leader.y = 0;
						leader.vy = 0;
						leader.ay = 0;
					}
					repaint();
					try {
						sleep(30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();

		while(true){
			for(Agent agent : group.getAgents()){
				Separation.update(group, agent);
				Cohesion.update(group, agent);
				Alignment.update(group, agent);
				Arrival.update(group, agent);
				agent.update(DT);
				group.updateCentroid();
			}
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class TestPanel extends JPanel {

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(Color.black);
			g2d.fillPolygon(getPolygon((int) leader.x, (int) leader.y));
			g2d.setColor(Color.blue);
			for(Agent agent : group.getAgents()){
				g2d.fillPolygon(getPolygon((int) agent.x, (int) agent.y));
			}
		}

		private Polygon getPolygon(int x, int y) {
			int xpts[] = {x, x - 4, x + 4};
			int ypts[] = {y, y + 12, y + 12};
			return new Polygon(xpts, ypts, 3);
		}

	}

	public static void main(String... args) {
		new AITest();
	}

	public void keyPressed(KeyEvent e) {
		char key = ("" + e.getKeyChar()).toLowerCase().charAt(0);
		switch (key) {
		case 'w':
			leader.vy = -V_MAX;
			break;
		case 's':
			leader.vy = V_MAX;
			break;
		case 'a':
			leader.vx = -V_MAX;
			break;
		case 'd':
			leader.vx = V_MAX;
			break;
		default:
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
		char key = ("" + e.getKeyChar()).toLowerCase().charAt(0);
		switch (key) {
		case 'w': case 's':
			leader.vy = 0;
			break;
		case 'a': case 'd':
			leader.vx = 0;
			break;
		default:
			break;
		}
	}
	public void keyTyped(KeyEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
		Agent agent = new Agent(e.getX(), e.getY());
		group.add(agent);
	}

	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mousePressed(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
	}

}
