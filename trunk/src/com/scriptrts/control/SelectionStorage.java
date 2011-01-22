package com.scriptrts.control;
public class SelectionStorage {
    private HashMap<Integer, Selection> map = new HashMap<Integer, Selection>(100);

    public static void store(Selection s, int id){
        map.put(s, id);
    }

    public static Selection retrieve(int id){
        return map.get(new Integer(id));
    }
}
