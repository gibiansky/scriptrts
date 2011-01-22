package com.scriptrts.control;
public class Selection implements Iterable<Entity> {

    /* Inner container */
    private ArrayList<Entity> objects = new ArrayList<MapElement>(10);

    public Selection(){
        super();
    }

    /* Global selections */
    public static Selection visibleConstructs(){

    }

    public static Selection pastConstructs(){

    }

    public static Selection visibleTerrain(){

    }

    public static Selection pastTerrain(){

    }

    /* Combine selections */
    public static Selection combine(Selection one, Selection two){

    }

}
