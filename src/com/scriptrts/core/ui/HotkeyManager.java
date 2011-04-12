package com.scriptrts.core.ui;

import java.util.ArrayList;

/**
 * Manages hotkeys for game
 */
public class HotkeyManager {
    /**
     * All registered hotkeys
     */
    private static ArrayList<Action> storedActions = new ArrayList<Action>();
    private static ArrayList<int[]> storedKeycodeCombinations = new ArrayList<int[]>();

    /**
     * Register a new hotkey
     * @param action action to execute upon the hotkey
     * @param args all keycodes that must be pressed for hotkey to be executed
     */
    public static void registerHotkey(Action action, int... args){
        storedActions.add(action);
        storedKeycodeCombinations.add(args);
        
        /* Register all keycodes if necessary */
        for(int keycode : args)
            InputManager.getInputManager().registerKeyCode(keycode);
    }

    public static void update(){
        for(int i = 0; i < storedKeycodeCombinations.size(); i++)
            /* If this hotkey is pressed */
            if(satisfied(storedKeycodeCombinations.get(i))){
                /* Execute the action */
                storedActions.get(i).execute();

                /* Clear all keys */
                for(int keycode : storedKeycodeCombinations.get(i))
                    InputManager.getInputManager().clearKeyCodeFlag(keycode);
            }
    }

    /**
     * Check if all keys in the array are pressed
     * @return true if all keys are pressed
     */
    private static boolean satisfied(int[] keycodes){
        for(int keycode : keycodes)
            if(!InputManager.getInputManager().getKeyCodeFlag(keycode))
                return false;
        return true;
    }
}
