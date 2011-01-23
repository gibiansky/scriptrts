package com.scriptrts.control;

import com.scriptrts.game.*;
import java.util.*;

public class Selection {

    /* Inner container */
    private ArrayList<Entity> objects = new ArrayList<Entity>(10);

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
        return null;
    }

}
