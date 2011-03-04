package com.scriptrts.control;

import java.util.*;

import com.scriptrts.game.*;

/**
 * A selection of units and other map entities
 */
public class Selection {

    /**
     * Inner container used to store the units
     */
    private ArrayList<SimpleUnit> objects = new ArrayList<SimpleUnit>();

    /**
     * Current selection (units currently selected by the player)
     */
    private static Selection current = new Selection();

    /**
     * Create a new selection
     */
    public Selection(){
        super();
    }
    
    /**
     * The selection currently selected by the player
     */
    public static Selection current(){
        return current;
    }

    /*
    public static Selection mine(){
        return null;
    }
    public static Selection enemy(){
        return null;
    }
    public static Selection ally(){
        return null;
    }

    public static Selection all(){
        return null;
    }
    public static Selection terrain(){
        return null;
    }
    */

    /**
     * Combine two selections
     * @param one first selection
     * @param two second selection
     * @return union of the two selections
     */
    public static Selection combine(Selection one, Selection two){
        Selection selection = new Selection();
        selection.add(one.getList());
        selection.add(two.getList());
        return selection;
    }

    /** 
     * Add a unit to a selection 
     * @param unit the unit to add
     */
    public void add(SimpleUnit unit){
        /* Don't add duplicate objects to a selection */
        if(!objects.contains(unit))
            objects.add(unit);
    }

    /**
     * Add a collection of units to a selection
     * @param units a java Collection of units to add
     */
    public void add(Collection<SimpleUnit> units){
        for(SimpleUnit u : units)
            add(u);
    }

    /**
     * Remove a unit from a selection
     * @param units the unit to remove
     */
    public void remove(SimpleUnit unit){
        objects.remove(unit);
    }

    /**
     * Clear a selection (remove all units in it).
     */
    public void clear(){
        objects.clear();
    }

    /**
     * Get the selection as a list of units. Modifying this list will modify the selection.
     * @return mutable list of units
     */
    public List<SimpleUnit> getList(){
        return objects;
    }

    /**
     * Check if this selection contains a unit
     * @param unit the unit to check
     * @return true if the unit is in the selection, false otherwise
     */
    public boolean contains(SimpleUnit unit){
        return objects.contains(unit);
    }
    
    public ArrayList<SimpleUnit> getObjects() {
		return objects;
	}
	
	public Selection clone()
	{
		Selection s = new Selection();
		s.add(this.getObjects());
		return s;
	}
	
	public static void replaceCurrent(Selection s)
	{
		current = s;
	}
}
