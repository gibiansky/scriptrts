package com.scriptrts.game;

import java.awt.Point;


/**
 * The shape of the unit, defined by what points it takes up 
 * around the center point.
 */
public class UnitShape {
    /**
     * An array of point arrays, with each element being an array that corresponds to the shape
     * of the unit when it is facing in each of the 8 directions.
     */
    Point[][] shapes = new Point[8][];
    static Point[] p7 = new Point[]{new Point(-3,-3), new Point(-3,-2), new Point(-3,-1), new Point(-3,0), new Point(-3,1), new Point(-3,2), new Point(-3,3), new Point(-2,-3), new Point(-2,-2), new Point(-2,-1), new Point(-2,0), new Point(-2,1), new Point(-2,2), new Point(-2,3), new Point(-1,-3), new Point(-1,-2), new Point(-1,-1), new Point(-1,0), new Point(-1,1), new Point(-1,2), new Point(-1,3), new Point(0,-3), new Point(0,-2), new Point(0,-1), new Point(0,0), new Point(0,1), new Point(0,2), new Point(0,3), new Point(1,-3), new Point(1,-2), new Point(1,-1), new Point(1,0), new Point(1,1), new Point(1,2), new Point(1,3), new Point(2,-3), new Point(2,-2), new Point(2,-1), new Point(2,0), new Point(2,1), new Point(2,2), new Point(2,3), new Point(3,-3), new Point(3,-2), new Point(3,-1), new Point(3,0), new Point(3,1), new Point(3,2), new Point(3,3)};
    static Point[] p5 = new Point[]{new Point(-2,-2), new Point(-2,-1), new Point(-2,0), new Point(-2,1), new Point(-2,2), new Point(-1,-2), new Point(-1,-1), new Point(-1,0), new Point(-1,1), new Point(-1,2), new Point(0,-2), new Point(0,-1), new Point(0,0), new Point(0,1), new Point(0,2), new Point(1,-2), new Point(1,-1), new Point(1,0), new Point(1,1), new Point(1,2), new Point(2,-2), new Point(2,-1), new Point(2,0), new Point(2,1), new Point(2,2)};
    
    
    public static final UnitShape SHAPE_VOLCANO = new UnitShape(
    	new Point[][]{
    		new Point[]{new Point(0,0), new Point(0,-1), new Point(-1,-1), new Point(-2,-1), new Point(-1,0), new Point(-2,0), new Point(-2,-2), new Point(-3,-2), new Point(-3,-3), new Point(-2,-3), new Point(-2,-4), new Point(-2,-5), new Point(-1,-6), new Point(-1,-5), new Point(0,-6), new Point(0,-7), new Point(1,-7), new Point(2,-7), new Point(2,-8), new Point(3,-8), new Point(4,-8), new Point(5,-7), new Point(5,-8), new Point(6,-7), new Point(6,-6), new Point(7,-6), new Point(7,-5), new Point(7,-4), new Point(6,-4), new Point(6,-3), new Point(6,-2), new Point(7,-3), new Point(5,-2), new Point(5,-1), new Point(6,-1), new Point(5,0), new Point(5,1), new Point(4,1), new Point(3,2), new Point(2,2), new Point(1,2), new Point(0,2), new Point(0,1), new Point(-1,1), new Point(-3,-1), new Point(1,1), new Point(2,1), new Point(3,1), new Point(4,0), new Point(4,-1), new Point(5,-3), new Point(5,-4), new Point(6,-5), new Point(5,-6), new Point(5,-5), new Point(4,-6), new Point(4,-7), new Point(3,-7), new Point(2,-6), new Point(1,-6), new Point(0,-5), new Point(0,-4), new Point(-1,-4), new Point(-1,-3), new Point(1,-4), new Point(0,-3), new Point(0,-2), new Point(-1,-2), new Point(1,-1), new Point(1,0), new Point(3,-1), new Point(3,0), new Point(2,0), new Point(1,-2), new Point(2,-2), new Point(2,-1), new Point(4,-2), new Point(4,-3), new Point(4,-4), new Point(3,-4), new Point(3,-2), new Point(3,-3), new Point(2,-3), new Point(2,-4), new Point(2,-5), new Point(3,-6), new Point(4,-5), new Point(3,-5), new Point(1,-5), new Point(1,-3)},
    		null, null, null, null, null, null, null
    	}
    );
    
    public static final UnitShape SHAPE_7x7 = new UnitShape(
    	new Point[][]{
    		p7, p7, p7, p7, p7, p7, p7, p7
    	}
    );
    
    public static final UnitShape SHAPE_5x5 = new UnitShape(
    	new Point[][]{
    		p5, p5, p5, p5, p5, p5, p5, p5
    	}
    );
    
    /**
     * A two-by-one elongated unit shape
     */
    public static final UnitShape SHAPE_2x1 = new UnitShape(
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
    public static final UnitShape SHAPE_1x1 = new UnitShape(
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
