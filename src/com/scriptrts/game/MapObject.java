package com.scriptrts.game;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import com.scriptrts.core.ui.Sprite;

/**
 * Unit class which only implements most basic functions.
 */
public class MapObject implements Serializable {

    /**
     * The Entity that this MapObject represents
     */
    private Entity entity;



    /**
     * The shape this unit takes up (which squares it uses)
     */
    private UnitShape shape;
    
    /**
     * The sprites used to display this unit.
     */
    private transient Sprite[] sprites;

    /**
     * Which sprite to display.
     */
    private SpriteState state;

    /**
     * Art image to display in selections
     */
    private transient BufferedImage art;

    /**
     * Unit ID
     */
    private int id;

    /**
     * Unit ID counter
     */
    private static int idCounter = 0;

    

    /**
     * How far the unit has progressed in its current tile movement.
     */
    private double animCounter = 0;

    /**
     * How far the current sprite animation has progressed (in frames)
     */
    private int spriteAnimationFrameCount = 0;

    /**
     * Create a new unit
     * @param p player to whom this unit owes allegiance
     * @param sprites array of sprites used to display the image
     * @param speed unit's speed of movement
     * @param x unit's starting location x coordinate
     * @param y unit's starting location y coordinate
     * @param direction direction in which the unit is facing originally
     * @param shaped if false, this unit takes up one square; if true, it can take up more.
     */
    public MapObject(Sprite[] sprites, BufferedImage artImg, boolean shaped, Entity entity) {
        this.sprites = sprites;
        this.id = idCounter;
        idCounter++;

        state = SpriteState.Idle;

        if(shaped)
            shape = UnitShape.SHAPE_2x1;
        else
            shape = UnitShape.SHAPE_1x1;

        this.art = artImg;

        this.entity = entity;
    }


    /**
     * Create a new unit that takes up one space
     * @param sprites array of sprites used to display the image
     */
    public MapObject(Sprite[] sprites, BufferedImage artImg, Entity entity) {
        this(sprites, artImg, true, entity);
    }

    /**
     * Create a blank MapObject
     */
    public MapObject(){
        this(null, null, false, null);
    }

    

    /**
     * Increment the sprite animation frame count
     */
    public void progressSpriteAnimation(){
        spriteAnimationFrameCount ++;
    }

    /**
     * Reset the sprite animation frame count to zero
     */
    public void resetSpriteAnimation(){
        spriteAnimationFrameCount = 0;
    }

    /**
     * Get the current sprite animation frame
     * @return frame on which the sprite animation is
     */
    public int getSpriteAnimation(){
        return spriteAnimationFrameCount;
    }

    /**
     * Set the sprite animation frame count to a given frame
     * @param f frame to set sprite animation to
     */
    public void resetSpriteAnimation(int f){
        spriteAnimationFrameCount = f;
    }
    
    public Entity getEntity() {
        return entity;
    }

    /**
     * Set this object's state to another object's
     */
    public void setParameters(MapObject source){
        state = source.state;
        id = source.id;
        animCounter = source.animCounter;
        animCounter = 0;
    }

    public void setParameters(SpriteState ustate, int uID){
        state = ustate;
        id = uID;
        animCounter = 0;
    }

    /**
     * Get the art image of this unit.
     * @return image containing art for this unit
     */
    public BufferedImage getArt(){
        return art;
    }

 

    /**
     * Find how far the animation has progressed
     * @return how many far the animation has progressed (double from 0 to 1)
     */
    public double getAnimationCounter(){
        return animCounter;
    }

    /**
     * Increments the animation counter
     * @param inc how much to increment the counter by
     * @return how many tile movements it finished 
     * */
    public int incrementAnimationCounter(double inc){
        animCounter += inc;

        if(animCounter >= 1){
            int retVal = (int) (animCounter);
            animCounter -= retVal;
            return retVal;
        }

        return 0;

    }

    

    /**
     * Get the unit ID
     * @return unique id of unit
     */
    public int getID(){
        return id;
    }

    /**
     * Get the current sprite state
     * @return the sprite state
     */
    public SpriteState getState() {
        return state;
    }

    /**
     * Get the shape of this unit
     * @param facing determines which direction this unit should be facing to get the shape
     * @return an array of points representing offsets from the unit center
     */
    public Point[] getShape(Direction facing){
        return shape.getShape(facing);
    }

    /**
     * Get the current shape of the unit, using the current facing direction.
     * @return an array of points representing offsets from the unit center
     */
    public Point[] getCurrentShape(){
        if(entity instanceof Unit)
            return getShape(((Unit) entity).getFacingDirection());
        return null;
    }

    /**
     * Get the sprites used by this image
     * @return array of sprites this unit may use
     */
    public Sprite[] getSprites(){
        return sprites;
    }

    /**
     * Set the sprite state
     * @param state the state to set
     */
    public void setState(SpriteState state) {
        this.state = state;

        /* Reset sprite animation */
        resetSpriteAnimation();
    }




    /**
     * Get all the x coordinates of the current shape
     * @return array of x coordinates
     */
    int[] allX = null;
    public int[] getAllX(){
        if(!(entity instanceof Unit))
            return null;
        if(allX == null)
            allX = new int[shape.getSize()];

        Point[] pts = shape.getShape(((Unit) entity).getFacingDirection());
        for(int i = 0; i < pts.length; i++)
            allX[i] = pts[i].x + entity.getX();

        return allX;
    }

    /**
     * Get all the y coordinates of the current shape
     * @return array of y coordinates
     */
    int[] allY = null;
    public int[] getAllY(){
        if(!(entity instanceof Unit))
            return null;
        
        if(allY == null)
            allY = new int[shape.getSize()];

        Point[] pts = shape.getShape(((Unit) entity).getFacingDirection());
        for(int i = 0; i < pts.length; i++)
            allY[i] = pts[i].y + entity.getY();

        return allY;
    }

    

    /**
     * Get the current sprite
     * @return the currentSprite
     */
    public Sprite getCurrentSprite() {
        if(!(entity instanceof Unit)){
            System.out.println("whoa wtf");
            return null;
        }
        
        int index = ((Unit) entity).getFacingDirection().ordinal() + Direction.values().length * getState().ordinal();
        return sprites[index];
    }



    /**
     * Check if this unit is the same unit as another one, based on ID.
     * @param unit unit to check against
     * @return true if the unit is the same, false otherwise
     */
    public boolean equals(MapObject unit){
        return unit.id == id;
    }

}

/**
 * The shape of the unit, defined by what points it takes up 
 * around the center point.
 */
class UnitShape implements Serializable {
    /**
     * An array of point arrays, with each element being an array that corresponds to the shape
     * of the unit when it is facing in each of the 8 directions.
     */
    Point[][] shapes = new Point[8][];

    /**
     * A two-by-one elongated unit shape
     */
    static final UnitShape SHAPE_2x1 = new UnitShape(
            new Point[][]{
                /* North */
                new Point[]{new Point(0,0), new Point(0, -1)},
                 /* Northeast */
                 new Point[]{new Point(0,0), new Point(-1, -1)},
                 /* East */
                 new Point[]{new Point(0,0), new Point(-1, 0)},
                 /* Southeast */
                 new Point[]{new Point(0,0), new Point(-1, 1)},
                 /* South */
                 new Point[]{new Point(0,0), new Point(0, 1)},
                 /* Southwest */
                 new Point[]{new Point(0,0), new Point(1, 1)},
                 /* West */
                 new Point[]{new Point(0,0), new Point(1, 0)},
                 /* Northwest */
                 new Point[]{new Point(0,0), new Point(1, -1)}
            }
            );

    /**
     * A simple shape composed of one square
     */
    static final UnitShape SHAPE_1x1 = new UnitShape(
            new Point[][]{
                /* North */
                new Point[]{new Point(0,0)},
                 /* Northeast */
                 new Point[]{new Point(0,0)},
                 /* East */
                 new Point[]{new Point(0,0)},
                 /* Southeast */
                 new Point[]{new Point(0,0)},
                 /* South */
                 new Point[]{new Point(0,0)},
                 /* Southwest */
                 new Point[]{new Point(0,0)},
                 /* West */
                 new Point[]{new Point(0,0)},
                 /* Northwest */
                 new Point[]{new Point(0,0)}
            }
            );

    /**
     * Create a new unit shape
     * @param shps the points in the shape, with each point being an offset from the unit center
     */
    public UnitShape(Point[][] shps){
        super();
        shapes = shps;
    }

    /**
     * Get the shape of the unit when it is facing a given direction
     * @param facing which direction it's facing
     * @return point array denoting unit shape
     */
    public Point[] getShape(Direction facing){
        return shapes[facing.ordinal()];
    }

    /**
     * Get how many squares this unit takes up
     * @return how many squares this unit takes up when facing North
     */
    public int getSize(){
        return getShape(Direction.North).length;
    }
}