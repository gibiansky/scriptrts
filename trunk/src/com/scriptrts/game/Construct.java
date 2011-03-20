package com.scriptrts.game;

public abstract class Construct extends Entity {
   

    /**
     * Player who controls this unit
     */
    protected Player player;
    
    /**
     * Get the player who currently controls this construct
     */
    public Player getAllegiance(){
        return player;
    }
    
    /**
     * Set the player who currently controls this construct
     */
    public void setAllegiance(Player p){
        this.player = p;
    }
    
    /**
     * The maximum health the construct can have
     */
    protected int maxHealth;

    /**
     * The current health of the construct
     */
    protected int health;

    /**
     * Whether the construct is alive
     */
    private boolean alive = true;
    
    /**
     * The sight radius of the construct
     */
    protected int visibilityRadius;
    
    /**
     * Returns the sight radius of the unit
     * @return the sight radius
     */     
    public int getVisibilityRadius() {
        return visibilityRadius;
    }

    /**
     * Sets the sight radius of the unit
     * @param the new radius
     */
    public void setVisibilityRadius(int radius) {
        visibilityRadius = radius;
    }
    
    /**
     * Get the maximum health of the unit
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Get the current health of the unit
     */
    public int getHealth() {
        return health;
    }

    /**
     * Set the current health of the unit
     * @param health the health to set
     */
    public void setHealth(int health) {
        this.health = health;
    }

    /**
     * Set the maximum health of the unit
     * @param maxHealth the maximum health to set
     */
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isAlive() {
        return alive;
    }
    
    /* protected int hitpoints;
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
    */

}
