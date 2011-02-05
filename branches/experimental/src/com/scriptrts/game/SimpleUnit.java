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
    private boolean selected;
    private Queue<Direction> path = new LinkedList<Direction>();
    private double animCounter = 0;

    public SimpleUnit(Sprite[] sprites, int speed, int x, int y, Direction direction) {
        this.sprites = sprites;
        this.speed = speed;
        this.x = x;
        this.y = y;
        this.direction = null;
        previousDirection = direction;
        state = SpriteState.Idle;


        /* LOLLER CATS */
        addToPath(Direction.East);
        addToPath(Direction.East);
        addToPath(Direction.East);
        addToPath(Direction.East);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);
        addToPath(Direction.North);

    }

    public boolean isSelected(){
        return selected;
    }
    public void select(){
        selected = true;
    }
    public void deselect(){
        selected = false;
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
        if(direction != null)
            return sprites[direction.ordinal()];
        else
            return sprites[previousDirection.ordinal()];
    }

    private class UnitShape {
        Point[][] shapes = new Point[8][];

        public void setShape(Point[] shp, Direction facing){
            shapes[facing.ordinal()] = shp;
        }

        public Point[] getShape(Direction facing){
            return shapes[facing.ordinal()];
        }
    }
}
