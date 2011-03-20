package com.scriptrts.game;
public abstract class Building extends Construct {
    

    public boolean isPassable(Construct c){
        return c instanceof Unit && this.player.equals(c.player);
    }
}
