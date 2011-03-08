package com.scriptrts.game;

import java.awt.Color;

public class Player {
    /* Available default colors */
    public static Color[] COLORS = {
        Color.RED, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.BLACK, Color.GRAY, Color.GREEN, Color.ORANGE
    };

    /**
     * Player's unique name
     */
    private String name;

    /**
     * Player's unique color
     */
    private Color color;
    
    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
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
        return "Player " + name + " (" + color + ")";

    }
}
