package com.scriptrts.core;


import com.scriptrts.game.*;

/**
 * Describes the location of a unit inside a unit tile.
 *
 */
public enum UnitLocation {
             Northwest,
        West,           North,
    Southwest,  Center,     Northeast,
        South,          East,
             Southeast;

    public UnitLocation move(Direction d){
        final UnitLocation[][] moveMapping = {
            /* Northwest */
            {Southwest, South, North, Center, West, East, Northeast, Southeast},
            /* West */
            {Northwest, North, Center, South, West, Southwest, East, Northeast},
            /* North */
            {South, Southeast, Northeast, East, Center, West, Northwest, Southwest},
            /* Southwest */
            {West, Center, South, North, Northwest, Northeast, Southeast, East},
            /* Center */
            {North, Northeast, East, Southeast, South, Southwest, West, Northwest},
            /* Northeast */
            {Southeast, Southwest, Northwest, West, East, Center, North, South},
            /* South */
            {Center, East, Southeast, Northeast, North, Northwest, Southwest, West},
            /* East */
            {Northeast, Northwest, West, Southwest, Southeast, South, Center, North},
            /* Southeast */
            {East, West, Southwest, Northwest, Northeast, North, South, Center}
        };

        return moveMapping[ordinal()][d.ordinal()];

    }
}
