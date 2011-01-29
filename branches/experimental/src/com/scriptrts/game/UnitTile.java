package com.scriptrts.game;

import com.scriptrts.core.UnitLocation;
public class UnitTile {
    public SimpleUnit[] units;

    public UnitTile(){
        units = new SimpleUnit[UnitLocation.values().length];
    }

    public boolean isEmpty(){
        for(SimpleUnit u : units)
            if(u != null)
                return false;

        return true;
    }

    public void removeUnit(SimpleUnit unit){
        for(int i = 0; i < units.length; i++){
            if(units[i] == unit)
                units[i] = null;
        }
    }

    public void addUnit(SimpleUnit unit){
        if(unit != null){
            units[unit.getUnitLocation().ordinal()] = unit;
        }
    }
}
