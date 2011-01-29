package com.scriptrts.game;
public class Resource extends Entity {
    private int remaining;
    private ResourceType type;

    public boolean isPassable(Construct c){
        return false; //TODO unless air unit
    }
}
