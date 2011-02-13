package com.scriptrts.combat;

public class Action {
    private Actions a;
    
    public Action(Actions a){
        this.a = a;
    }
    
    public boolean equals(Action a){
        return this.a == a.a;
    }
    
    public Actions getAction(){
        return a;
    }
    
    public Action clone(){
        return new Action(a);
    }
  
    public enum Actions{
      CHANGE_STANCE,
      CHANGE_ATTACK_TYPE,
      MOVE,
      CANCEL,
      HOLD_GROUND,
      PATROL,
      DEFEND,
      FOLLOW,
      ATTACK
    };
}
