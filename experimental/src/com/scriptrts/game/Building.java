package com.scriptrts.game;
public abstract class Building extends Construct {
    

    public boolean isPassable(Construct c){
        return this.allegiance.equals(c.allegiance);
    }
}
