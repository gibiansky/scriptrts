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
            {},
            /* West */
            {},
            /* North */
            {},
            /* Southwest */
            {},
            /* Center */
            {},
            /* Northeast */
            {},
            /* South */
            {},
            /* East */
            {},
            /* Southeast */
            {}
        };

        return moveMapping[ordinal()][d.ordinal()];

    }
}
