package com.scriptrts.game;

import java.awt.Point;

import com.scriptrts.core.ui.Sprite;

public abstract class Entity {
    /**
     * The sprites used to display this unit.
     */
    private transient Sprite[] sprites;
    
    /**
     * The MapObject that represents this entity.
     */
    protected MapObject mapObject;
    
    /**
     * Get the sprites used by this image
     * @return array of sprites this unit may use
     */
    public Sprite[] getSprites(){
        return sprites;
    }
    
    /**
     * Set the sprites
     * @param sprites the sprites to set to this entity
     */
    public void setSprites(Sprite[] sprites){
        this.sprites = sprites;
    }
    
    public MapObject getMapObject() {
        return mapObject;
    }
    
    // Location of top left corner
    protected int x;
    protected int y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
    

    
    /**
     * Get the location of the entity
     * @return Point p -- the location of this unit
     */
    public Point getLocation(){
        return new Point(x, y);
    }

   
    /**
     * Determines whether this is passable or transparent to c.
     * Specific implementation depends on Construct type.
     * @param c Construct attempting to determine whether this is passable.
     * @return true if this is passable, false otherwise.
     */
    public abstract boolean isPassable(Construct c);

    public void setLocation(Point point) {
        this.x = point.x;
        this.y = point.y;
    }
 
    public enum EntityType{
        UNIT,
        BUILDING;
    }
   
}
