package com.scriptrts.control;

import java.util.*;
import java.util.Collection;

import com.scriptrts.game.*;

public class Selection {

    /* Inner container */
    private Set<SimpleUnit> objects = new HashSet<SimpleUnit>();

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

    public boolean contains(SimpleUnit unit){
        return objects.contains(unit);
    }
}
