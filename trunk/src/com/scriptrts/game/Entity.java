package com.scriptrts.game;

import java.awt.Point;

import com.scriptrts.core.ui.Sprite;

public abstract class Entity {
    /**
     * The sprites used to display this unit.
     */
    private transient Sprite[] sprites;
    
    /**
     * X coordinate of entity on the map
     */
    protected int x;

    /**
     * Y coordinate of entity on the map 
     */
    protected int y;
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
    
    /**
     * Get map object used to draw this entity on the map
     * @return entity map object
     */
    public MapObject getMapObject() {
        return mapObject;
    }
    

    /**
     * Get x location
     * @return x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Set x location
     * @param x new x coordinate
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Get y location
     * @return y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Set y location
     * @param y new y coordinate
     */
    public void setY(int y) {
        this.y = y;
    }
    
    
    /**
     * Get the location of the entity
     * @return the Point location of this unit
     */
    public Point getLocation(){
        return new Point(x, y);
    }

    /**
     * Set the entity location
     * @param point new location on the map
     */
    public void setLocation(Point point) {
        setX(point.x);
        setY(point.y);
    }
 
   
    /**
     * Determines whether this is passable or transparent to c.
     * Specific implementation depends on Construct type.
     * @param c Construct attempting to determine whether this is passable.
     * @return true if this is passable, false otherwise.
     */
    public abstract boolean isPassable(Construct c);

    public enum EntityType {
        UNIT,
        BUILDING;
    }
   
}
