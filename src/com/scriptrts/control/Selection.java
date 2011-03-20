package com.scriptrts.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.scriptrts.core.ui.SelectionArea;
import com.scriptrts.game.MapObject;

/**
 * A selection of units and other map entities
 */
public class Selection {

    /**
     * Inner container used to store the units
     */
    private ArrayList<MapObject> objects = new ArrayList<MapObject>();

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
    public void add(MapObject unit){
        /* Don't add duplicate objects to a selection */
        if(!objects.contains(unit))
            objects.add(unit);

        if(this == Selection.current())
            SelectionArea.selectionChanged();
    }

    /**
     * Add a collection of units to a selection
     * @param units a java Collection of units to add
     */
    public void add(Collection<MapObject> units){
        for(MapObject u : units)
            add(u);
    }

    /**
     * Remove a unit from a selection
     * @param units the unit to remove
     */
    public void remove(MapObject unit){
        objects.remove(unit);

        if(this == Selection.current())
            SelectionArea.selectionChanged();
    }

    /**
     * Clear a selection (remove all units in it).
     */
    public void clear(){
        objects.clear();

        if(this == Selection.current())
            SelectionArea.selectionChanged();
    }

    /**
     * Get the selection as a list of units. Modifying this list will modify the selection.
     * @return mutable list of units
     */
    public List<MapObject> getList(){
        return objects;
    }

    /**
     * Check if this selection contains a unit
     * @param unit the unit to check
     * @return true if the unit is in the selection, false otherwise
     */
    public boolean contains(MapObject unit){
        return objects.contains(unit);
    }
    
    public ArrayList<MapObject> getObjects() {
		return objects;
	}
	
	public Selection clone()
	{
		Selection s = new Selection();
		s.add(this.getObjects());
		return s;
	}
	
	public boolean equals(Selection s){
		for(MapObject x: s.objects){
			if(!this.contains(x))
				return false;
		}
		return true;
	}
	
	public boolean isEmpty()
	{
		return this.objects.isEmpty();
	}
	
	public static void replaceCurrent(Selection s)
	{
		if (!Selection.current().isEmpty())
            SelectionStorage.store(Selection.current(), 10);
		current = s;

        SelectionArea.selectionChanged();
	}
}
