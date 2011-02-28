package com.scriptrts.control;

import java.util.*;

import com.scriptrts.game.*;

public class Selection {

    /* Inner container */
    private ArrayList<SimpleUnit> objects = new ArrayList<SimpleUnit>();

    /* Static selections */
    private static Selection current = new Selection();

    public Selection(){
        super();
    }
    
    /* Managing player selections */
    public static Selection current(){
        return current;
    }
    public static Selection mine(){
        return null;
    }
    public static Selection enemy(){
        return null;
    }
    public static Selection ally(){
        return null;
    }

    /* Global selections */
    public static Selection all(){
        return null;
    }
    public static Selection terrain(){
        return null;
    }

    /* Combine selections */
    public static Selection combine(Selection one, Selection two){
        Selection selection = new Selection();
        selection.add(one.getList());
        selection.add(two.getList());
        return selection;
    }

    /* Add to a selection */
    public void add(SimpleUnit unit){
        if(!objects.contains(unit))
            objects.add(unit);
    }

    public void add(Collection<SimpleUnit> units){
        for(SimpleUnit u : units)
            add(u);
    }

    public void remove(SimpleUnit unit){
        objects.remove(unit);
    }

    public void clear(){
        objects.clear();
    }

    public List<SimpleUnit> getList(){
        return objects;
    }

    public boolean contains(SimpleUnit unit){
        return objects.contains(unit);
    }
}
