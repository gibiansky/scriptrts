package com.scriptrts.combat;

import java.util.ArrayList;

public interface Hero {
    //Must have int experience
    public int getExperience();
    public void setExperience();
    
    // Must have int level
    public void levelUp();
    public int getLevel();
    public void setLevel();
    
    // Must have ArrayList<Skill> skills
    public void addSkill();
    public ArrayList<Skill> getSkills();
    public void setSkills();
    public void activateSkill();
}
