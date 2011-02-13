package com.scriptrts.control;

import java.util.ArrayList;
import java.util.Collection;

import com.scriptrts.game.*;

public class Selection {

    /* Inner container */
    private ArrayList<SimpleUnit> objects = new ArrayList<SimpleUnit>(10);

    public Selection(){
        super();
    }

    /* Global selections */
    public static Selection allEntities(){
        return null;
    }
    public static Selection myEntities(){
        return null;
    }
    public static Selection enemyEntities(){
        return null;
    }
    public static Selection allyEntities(){
        return null;
    }
    public static Selection terrainEntities(){
        return null;
    }

    /* Combine selections */
    public static Selection combine(Selection one, Selection two){
        Selection selection = new Selection();
        selection.add(one.getCollection());
        selection.add(two.getCollection());
        return selection;
    }

    /* Add to a selection */
    public void add(SimpleUnit unit){
        objects.add(unit);
    }

    public void add(Collection<SimpleUnit> units){
        objects.addAll(units);
    }

    public void remove(SimpleUnit unit){
        objects.remove(unit);
    }

    public void clear(){
        objects.clear();
    }

    public Collection<SimpleUnit> getCollection(){
        return objects;
    }
}
