package com.scriptrts.game;

import java.util.List;

/**
 * Unit object for unit types
 */
public class UnitType {
    /**
     * Counter used to initialize IDs of new units 
     */
    protected static int idCounter = 0;

    /**
     * ID of this unit (unique)
     */
    protected int id;

    /**
     * Unit class of this unit, i.e. Standard, Building, Terrain
     */
    protected UnitClass unitClass = UnitClass.Standard;

    /**
     * The unit type name
     */
    protected String name;

    /**
     * Number of hitpoints unit type starts with
     */
    protected int hitpoints;

    /**
     * The armor bonus of this unit
     */
    protected int armor;

    /**
     * The base attack of this unit
     */
    protected int attack;

    /**
     * The attack type of this unit
     */
    protected AttackType attackType;
    
    /**
     * The attributes this unit has
     */
    protected List<Attribute> attributes;

    /**
     * Check if the units are equal
     * @return true if the unit is the same instance
     */
    public boolean equals(UnitType u){
        return u.id == id;
    }

    /**
     * Check if this unit is a standard unit
     * @return true if this unit is a standard unit
     */
    public boolean isStandard(){
    	return unitClass == UnitClass.Standard;
    }
    
    /**
     * Check if this unit is a building
     * @return true if this unit is a building
     */
    public boolean isBuilding(){
        return unitClass == UnitClass.Building;
    }
    /**
     * Check if this unit is a part of the terrain
     * @return true if this unit is a part of the terrain
     */
    public boolean isTerrain(){
        return unitClass == UnitClass.Terrain;
    }

    /**
     * Get the name of the unit type
     * @return unit type name
     */
    public String name(){
        return name;
    }

    /**
     * Get the current number of hitpoints
     * @return current hitpoints
     */
    public int hitpoints(){
        return hitpoints;
    }

    /**
     * Check whether this unit is alive
     * @return true if hitpoints are above 0
     */
    public boolean alive(){
        return (hitpoints > 0);
    }

    /**
     * Get the armor bonus
     * @return armor
     */
    public int armor(){
        return armor;
    }

    /**
     * Get the base attack
     * @return base attack
     */
    public int attack(){
        return attack;
    }

    /**
     * Get the attack type
     * @return attack type
     */
    public AttackType attackType(){
        return attackType;
    }

    /**
     * Get the attributes of this unit
     * @return list of attributes
     */
    public List<Attribute> attributes(){
        return attributes;
    }

    /**
     * Add an attribute to a unit
     * @param a attribute to add
     */
    public void addAttribute(Attribute a){
        attributes().add(a);
    }

    /**
     * Remove an attribute from a unit
     * @param a attribute to remove
     */
    public void removeAttribute(Attribute a){
        attributes().remove(a);
    }

    /**
     * Calculate damage done by this unit to another unit
     * @param unit defending unit
     * @return damage dealt to opposing unit
     */
    public int damageDealtTo(Unit defending){
        /* Deal base damage */
        int damage = attack();

        /* Minus armor */
        damage -= defending.armor();

        /* Add up all anti-attribute bonuses */
        for(Attribute attribute : defending.attributes())
            damage += attackType().attackBonus(attribute);

        /* Minimum of 1 damage */
        if(damage <= 0)
            damage = 1;

        return damage;
    }
    
	/**
	 * Set the unit class
	 * @param unitClass the unitClass to set
	 */
	public void setUnitClass(UnitClass unitClass) {
		this.unitClass = unitClass;
	}

	/**
	 * Get the unit class
	 * @return the unitClass
	 */
	public UnitClass getUnitClass() {
		return unitClass;
	}
}
