package com.scriptrts.core.ui;
/**
 * Action that can be run on a hotkey press
 */
public class Action {
    /**
     * The name of this action. Used for display on the UI.
     */
    private String name;

    /**
     * Create a new action
     * @param name human-readable name for the action
     */
    public Action(String name){
        super();
        this.name = name;
    }

    /**
     * What to run on this action
     */
    public void execute(){

    }

    /**
     * Get the name of this action
     * @return name of the action
     */
    public String getName(){
        return name;
    }
}
