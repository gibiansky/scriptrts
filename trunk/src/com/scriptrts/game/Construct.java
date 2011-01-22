public abstract class Construct extends Entity {
    protected int hitpoints;
    protected Player allegiance;
    protected boolean vulnerable;

    /**
     * Determines whether this is passable or transparent to c.
     * Specific implementation depends on Construct type.
     * @param c Construct attempting to determine whether this is passable.
     * @return true if this is passable, false otherwise.
     */
    public abstract boolean isPassable(Construct c);
}
