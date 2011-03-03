package com.scriptrts.control;
import java.util.*;

/**
 * Data storage area used for storing global selections (per player)
 */
public class SelectionStorage {
    /**
     * Hash which associates selections with integers; usually accessed by Ctrl-<int>
     */
    private static HashMap<Integer, Selection> map = new HashMap<Integer, Selection>(100);

    /**
     * Store a selection associated with a number
     * @param s the selection to store
     * @param id the integer to associate it with
     */
    public static void store(Selection s, int id){
        map.put(id, s);
    }

    /**
     * Retrieve a stored selection
     * @param id the integer the desired selection was associated with
     * @return the selection associated with the parameter
     */
    public static Selection retrieve(int id){
        return map.get(new Integer(id));
    }
}
