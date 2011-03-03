package com.scriptrts.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class PathTester {
    private  boolean[][] map = 
    {{true, true, true, true, true, true, true, false, false, false, true, true, true, true, true, true, false, false, false, true},
     {true, true, true, true, true, true, true, false, false, false, true, true, true, true, true, false, false, false, false, false},
     {true, true, true, true, true, true, true, true, false, false, false, true, true, true, true, true, false, false, false, true},
     {true, true, true, true, true, true, true, true, true, false, false, false, true, true, true, true, true, false, false, false},
     {true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false},
     {true, true, true, true, true, true, true, true, true, true, false, true, true, true, true, true, true, true, true, false},
     {true, true, true, true, true, true, true, true, true, true, false, false, false, false, true, true, true, true, true, true},
     {true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, true, true, true},
     {true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, true, true, true, true, true},
     {true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, true, true, true, true, true}};
    // 0-indexed, from top left
    private  int x = 0;
    private  int y = 0;
    
    private Stack<Node> pos = new Stack<Node>();
    
    private  final int X = 3;
    private  final int Y = 3;

    // 0 1 2
    // 7 X 3 
    // 6 5 4  [x,y]
    static int[][] neighbors = new int[8][2];
    static double[] distances = new double[8];
    
    static ArrayList<Node> nodes = new ArrayList<Node>();
    
    public static void main(String... args){
       PathTester a =  new PathTester();
        while(a.x != a.X && a.y != a.Y){
            a.genNeighbors();
            a.genDistances();
            a.stackify();
        }
    }
    
    public  void genNeighbors(){
        neighbors[0] = new int[]{x-1, y-1};
        neighbors[1] = new int[]{x, y-1};
        neighbors[2] = new int[]{x+1, y-1};
        neighbors[3] = new int[]{x+1, y};
        neighbors[4] = new int[]{x+1, y+1}; 
        neighbors[5] = new int[]{x, y+1};
        neighbors[6] = new int[]{x-1, y+1};
        neighbors[7] = new int[]{x-1, y};

    }
    
    public  void genDistances(){
        for(int i = 0; i < 8; i++){
            if(neighbors[i][0] >= 0 && neighbors[i][1] >=0)
                distances[i] = Math.sqrt(Math.pow(neighbors[i][0],2) + Math.pow(neighbors[i][1],2));
            else
                distances[i] = Integer.MAX_VALUE;
        }
    }
    
    public void stackify(){
        nodes.retainAll(null);
        for(int i = 0; i < 8; i++)
            if(distances[i] >= 0)
                nodes.add(this.new Node(map[neighbors[i][1]][neighbors[i][0]],neighbors[i][0],neighbors[i][1],distances[i]));
        Collections.sort(nodes);
        for(Node node:nodes)
            pos.push(node);
        for(Node node : pos)
            System.out.println(node.getDistance());
    }
    
    private class Node implements Comparable<Node>{
      

        private boolean passable;
        private int x;
        private int y;
        private double distance;

        @Override
        public int compareTo(Node c) {
            return (int) -Math.signum(((Node)c).distance - this.distance);
            
        }
        
        private Node(boolean passable, int x, int y, double distance){
           this.passable = passable;
           this.x = x;
           this.y = y;
           this.distance = distance;
        }
        
        public boolean isPassable() {
            return passable;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public double getDistance() {
            return distance;
        }
    }
}
