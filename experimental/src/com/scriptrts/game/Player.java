package com.scriptrts.game;

import java.awt.Color;
import java.util.EmptyStackException;
import java.util.Stack;

public class Player {
    private String name;
    private int id;
    private static int idCounter = 1;
    private Color color;
    
    private static Stack<Color> colors = new Stack<Color>();
    static{
        colors.push(Color.gray);
        colors.push(Color.blue);
        colors.push(Color.cyan);
        colors.push(Color.green);
        colors.push(Color.magenta);
        colors.push(Color.orange);
        colors.push(Color.pink);
        colors.push(Color.red);
        colors.push(Color.yellow);
    }

    public Player(String name) throws TooManyPlayersException{
        this.name = name;
        id = idCounter++;
        try{
            color = colors.pop();
        } catch(EmptyStackException e){
            throw new TooManyPlayersException();
        }
    }
    
    /**
     * Test for equality between Players, uses unique id;
     * @param p
     * @return
     */
    public boolean equals(Player p){
        return this.id == p.id;
    }
}
