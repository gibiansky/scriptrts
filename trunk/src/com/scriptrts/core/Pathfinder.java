package com.scriptrts.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Pathfinder extends JFrame {

	private static final int WIDTH = 600, HEIGHT = 600;
	private MyPanel panel;
	private Timer timer;

	private static final int N = 15;
	private static final int TILEW = WIDTH / N, TILEH = HEIGHT / N;
	private byte[][] map;

	private static final Point start = new Point(0, 0);
	private static final Point end = new Point(10, 3);

	public Pathfinder(String file) {
		
		if(map[start.x][start.y] == 0 || map[end.x][end.y] == 0) {
			System.out.println("WARNING: One of the end points is invalid");
			System.exit(1);
		}
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		pack();

		map = new byte[N][N];

		/* Read in map data */
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
			String[][] lines = new String[N][N];
			String line;
			int counter = 0;
			while((line = reader.readLine()) != null) {
				lines[counter] = line.split(" ");
				counter ++;
			}
			for(int i = 0; i < N; i++)
				for(int j = 0; j < N; j++)
					map[i][j] = Byte.parseByte(lines[i][j]);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		panel = new MyPanel();
		add(panel);
		setVisible(true);

		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				repaint();
			}
		}, 0, 75);
	}

	public static void main(String... argv) {
		new Pathfinder("resource/pathtest.txt");
	}

	public class MyPanel extends JPanel {
		@Override
		public void paint(Graphics g) {
			for (int i = 0; i < map.length; i++) {
				for (int j = 0; j < map[i].length; j++) {
					g.setColor(Color.black);
					g.drawRect(i * TILEW, j * TILEH, TILEW, TILEH);
					if(map[i][j] == 0)
						g.fillRect(i * TILEW, j * TILEH, TILEW, TILEH);
					if(i == start.x && j == start.y) {
						g.setColor(Color.blue);
						g.fillRect(i * TILEW, j * TILEH, TILEW, TILEH);
					} else if(i == end.x && j == end.y) {
						g.setColor(Color.red);
						g.fillRect(i * TILEW, j * TILEH, TILEW, TILEH);
					}
				}
			}
		}
	}
}
