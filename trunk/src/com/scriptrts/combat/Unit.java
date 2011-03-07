import java.util.List;

/**
 * Unit object for unit types
 */
public class Unit {
    /**
     * Counter used to initialize IDs of new units 
     */
    private static int idCounter = 0;

    /**
     * ID of this unit (unique)
     */
    private int id;

    /**
     * The unit type name
     */
    private String name;

    /**
     * Number of hitpoints unit type starts with
     */
    private int hitpoints;

    /**
     * The armor bonus of this unit
     */
    private int armor;

    /**
     * The base attack of this unit
     */
    private int attack;

    /**
     * The attack type of this unit
     */
    private AttackType attackType;
    
    /**
     * The attributes this unit has
     */
    private List<Attribute> attributes;

    /**
     * Check if the units are equal
     * @return true if the unit is the same instance
     */
    public boolean equals(Unit u){
        return u.id == id;
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
}
