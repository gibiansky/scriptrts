package com.scriptrts.combat;

import java.util.ArrayList;

public interface Support {
    
    // Must have ArrayList<Skill> skills
    public void addSkill();
    public ArrayList<Skill> getSkills();
    public void setSkills();
    public void activateSkill();
}
