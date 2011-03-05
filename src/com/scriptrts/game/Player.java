package com.scriptrts.game;

import java.awt.Color;

public class Player {
    private String name;
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
     * Test for equality between players using name
     * @param p player to test equality against
     * @return whether the players are equal
     */
    public boolean equals(Player p){
        return this.name.equals(p.getName());
    }
}
