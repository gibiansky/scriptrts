package com.scriptrts.game;

import java.util.Queue;
import java.awt.Point;
import java.util.LinkedList;
import java.util.Collections;

public class SimpleUnit {

    private Sprite[] sprites;
    private SpriteState state;
    private int speed;
    private int x, y;
    private Direction previousDirection;
    private Direction direction;
    private Queue<Direction> path = new LinkedList<Direction>();
    private double animCounter = 0;
    private UnitShape shape;
    private Point destination;

    public SimpleUnit(Sprite[] sprites, int speed, int x, int y, Direction direction, boolean shaped) {
        this.sprites = sprites;
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.direction = null;
        previousDirection = direction;
        state = SpriteState.Idle;

        if(shaped)
            shape = UnitShape.SHAPE_2x1;
        else
            shape = UnitShape.SHAPE_1x1;

        /* MAKE A RANDOM WEIRD PATH (TESTING!!!) */
        boolean circular = true;
        for(int i = 0; i < 50; i++){
            if(circular){
            if(i % 2 == 0){
                addToPath(Direction.East);
                addToPath(Direction.East);
                addToPath(Direction.East);
                addToPath(Direction.East);
                addToPath(Direction.North);
                addToPath(Direction.North);
                addToPath(Direction.North);
                addToPath(Direction.North);
                addToPath(Direction.West);
                addToPath(Direction.West);
                addToPath(Direction.West);
                addToPath(Direction.West);
                addToPath(Direction.South);
                addToPath(Direction.South);
                addToPath(Direction.South);
                addToPath(Direction.South);
            }
            else{
                addToPath(Direction.Northeast);
                addToPath(Direction.Northeast);
                addToPath(Direction.Northeast);
                addToPath(Direction.Northeast);
                addToPath(Direction.Northwest);
                addToPath(Direction.Northwest);
                addToPath(Direction.Northwest);
                addToPath(Direction.Northwest);
                addToPath(Direction.Southwest);
                addToPath(Direction.Southwest);
                addToPath(Direction.Southwest);
                addToPath(Direction.Southwest);
                addToPath(Direction.Southeast);
                addToPath(Direction.Southeast);
                addToPath(Direction.Southeast);
                addToPath(Direction.Southeast);
            }
            }
            else {
                addToPath(Direction.East);
                addToPath(Direction.East);
                addToPath(Direction.East);
                addToPath(Direction.East);
                addToPath(Direction.East);
                addToPath(Direction.East);
                addToPath(Direction.East);
                addToPath(Direction.East);
            }
        }
    }

    public SimpleUnit(Sprite[] sprites, int speed, int x, int y, Direction direction) {
        this(sprites, speed, x, y, direction, true);
    }

    public boolean isPassable(SimpleUnit u){
        return false;
    }

    public Point getDestination() {
        return destination;
    }

    public void setDestination(Point p){
        destination = p;
    }

    public void setDestination(int x, int y){
        setDestination(new Point(x, y));
    }

    public double getAnimationCounter(){
        return animCounter;
    }
    /* returns how many tile movements it finished */
    public int incrementAnimationCounter(double inc){
        animCounter += inc;

        if(animCounter >= 1){
            int retVal = (int) (animCounter);
            animCounter -= retVal;
            return retVal;
        }

        return 0;

    }

    public void resetAnimationCounter(){
        animCounter = 0;
    }


    /**
     * Update the direction based on the path the unit wants to take.
     */
    public void updateDirection(){
        if(direction != null)
            previousDirection = direction;

        /* If we have no more directions, stop. */
        if(path == null || path.peek() == null){
            direction = null;
        }
        else {
            direction = path.poll();
        }
    }

    /**
     * Look at where the unit will go next without updating the direction
     */
    public Direction peekNextDirection(){
        Direction next;

        if(path == null || path.peek() == null)
            next = null;
        else 
            next = path.peek();

        return next;
    }

    /**
     * Get the path this unit is going to take
     */
    public Queue<Direction> getPath(){
        return (Queue<Direction>) Collections.unmodifiableCollection(path);
    }

    /**
     * Set the path this unit is going to take
     */
    public void setPath(Queue<Direction> path){
        this.path =  path;
        if(direction == null && path != null && path.peek() != null)
            direction = path.poll();
    }

    public void clearPath(){
        path.clear();
    }

    /**
     * Append to the path this unit will take
     */
    public void addToPath(Direction d){
        if(direction == null)
            direction = d;
        else
            path.add(d);
    }

    /**
     * Append to the path this unit will take
     */
    public void addToPath(Queue<Direction> additionalPath){
        while(additionalPath != null && additionalPath.peek() != null)
            addToPath(additionalPath.poll());
    }

    /**
     * @return the state
     */
    public SpriteState getState() {
        return state;
    }

    public Point[] getShape(Direction facing){
        return shape.getShape(facing);
    }

    public Point[] getCurrentShape(){
        return getShape(getFacingDirection());
    }

    /**
     * @return array of sprites this unit may use
     */
    public Sprite[] getSprites(){
        return sprites;
    }

    /**
     * @param state the state to set
     */
    public void setState(SpriteState state) {
        this.state = state;
    }

    /**
     * @return the speed
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    int[] allX = null;
    public int[] getAllX(){
        if(allX == null)
            allX = new int[shape.getSize()];

        Point[] pts = shape.getShape(getFacingDirection());
        for(int i = 0; i < pts.length; i++)
            allX[i] = pts[i].x + getX();

        return allX;
    }

    int[] allY = null;
    public int[] getAllY(){
        if(allY == null)
            allY = new int[shape.getSize()];

        Point[] pts = shape.getShape(getFacingDirection());
        for(int i = 0; i < pts.length; i++)
            allY[i] = pts[i].y + getY();

        return allY;
    }

    /**
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * @return the currentSprite
     */
    public Sprite getCurrentSprite() {
        return sprites[getFacingDirection().ordinal()];
    }

    public Direction getFacingDirection(){
        if(direction != null)
            return direction;
        else
            return previousDirection;
    }

}

class UnitShape {
    Point[][] shapes = new Point[8][];

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

    public UnitShape(Point[][] shps){
        super();
        shapes = shps;
    }

    public void setShape(Point[] shp, Direction facing){
        shapes[facing.ordinal()] = shp;
    }

    public Point[] getShape(Direction facing){
        return shapes[facing.ordinal()];
    }

    public int getSize(){
        return getShape(Direction.North).length;
    }
}
