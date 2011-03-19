
package com.scriptrts.combat;
/**
 * Attack types which do different damage to different attributed units
 */
public enum AttackType {
    Piercing,
    Incendiary,
    Laser;

    /**
     * Get the attack bonus versus a unit with a certain attribute
     * @param a attribute of the defending unit
     * @return percentage attack bonus
     */
    public double attackBonus(Attribute a){
        switch(this){
            case Incendiary:
                if(a == Attribute.Biological) return 1.5;
                if(a == Attribute.Mechanical) return 0.5;
            default:
                return 0;
        }
    }
}
