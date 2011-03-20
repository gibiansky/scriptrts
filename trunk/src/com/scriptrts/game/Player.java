package com.scriptrts.game;

import java.awt.Color;
import java.io.Serializable;

import com.scriptrts.core.Main;

public class Player implements Serializable {
    /* Available default colors */
    public static Color[] COLORS = {
        Color.RED, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.BLACK, Color.GRAY, Color.GREEN, Color.ORANGE
    };

    /**
     * Player's unique ID
     */
    private int id;

    /**
     * Player's unique name
     */
    private String name;

    /**
     * Player's unique color
     */
    private Color color;

    /**
     * Stores visibility data about each map tile. 0 means unexplored,
     * 1 means no recent data, 2 means visible to some unit
     */
    private byte[][] visibilityGrid;
    
    public Player(String name, Color color, int id){
        this.name = name;
        this.color = color;
        this.id = id;

        int n = Main.getGame().getCurrentMap().getN() * MapObjectGrid.SPACES_PER_TILE;
        visibilityGrid = new byte[n][n];
    }
    
    public Player(String name, Color color) {
        this(name, color, (int)(Math.random() * 100000));
    }

    public byte[][] getVisibilityGrid() {
        return visibilityGrid;
    }

    /**
     * Get player name
     * @return player name
     */
    public String getName(){
        return name;
    }
     /**
     * Get player color
     * @return player color
     */
    public Color getColor(){
        return color;
    }

    /**
     * Get player id
     * @return player id
     */
    public int getID(){
        return id;
    }
 
    /**
     * Set player name
     */
    public void setName(String n){
        this.name = n;
    }
     /**
     * Set player color
     */
    public void setColor(Color c){
        color = c;
    }   

    /**
     * Set player id
     */
    public void setID(int i){
        id = i;
    }   

    /**
     * Test for equality between players using name
     * @param p player to test equality against
     * @return whether the players are equal
     */
    public boolean equals(Player p){
        return this.name.equals(p.getName());
    }

    /**
     * Print a player in string form
     */
    public String toString(){
        return "Player " + name + " (" + color + ") ID " + getID();

    }
}
