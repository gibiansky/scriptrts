package com.scriptrts.control;
import java.util.*;
public class SelectionStorage {
    private static HashMap<Integer, Selection> map = new HashMap<Integer, Selection>(100);

    public static void store(Selection s, int id){
        map.put(id, s);
    }

    public static Selection retrieve(int id){
        return map.get(new Integer(id));
    }
}
