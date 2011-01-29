package com.scriptrts.game;

public abstract class Construct extends Entity {
    protected int hitpoints;
    protected int curHitpoints;
    // Array in order to incorporate different types of armor.
    protected int[] armor;
    
    public void heal(int hp){
        curHitpoints = curHitpoints + hp > hitpoints ? hitpoints : curHitpoints + hp;
    }
    
    public void hurt(int hp){
        curHitpoints = curHitpoints - hp < 0 ? 0 : curHitpoints - hp;
    }
    
    
    public int getHitpoints() {
        return hitpoints;
    }
    public void setHitpoints(int hitpoints) {
        this.hitpoints = hitpoints;
    }
    public int getCurHitpoints() {
        return curHitpoints;
    }
    public void setCurHitpoints(int curHitpoints) {
        this.curHitpoints = curHitpoints;
    }
    public int[] getArmor() {
        return armor;
    }
    public void setArmor(int[] armor) {
        this.armor = armor;
    }
    public Player getAllegiance() {
        return allegiance;
    }
    public void setAllegiance(Player allegiance) {
        this.allegiance = allegiance;
    }
    public boolean isVulnerable() {
        return vulnerable;
    }
    public void setVulnerable(boolean vulnerable) {
        this.vulnerable = vulnerable;
    }
    public boolean isVisible() {
        return visible;
    }
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    protected Player allegiance;
    protected boolean vulnerable;
    protected boolean visible;

}
