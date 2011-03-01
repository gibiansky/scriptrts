from com.scriptrts.game import Direction as JDirection

# Create a direction enumeration. Directions can be accessed as direction.North, direction.East, etc
class direction: pass

# Populate all directions by copying the java Direction enum
for direct in JDirection.values():
    direction.__dict__[direct.name()] = direct

# Create a wrapper for the java Unit class
class Unit:
    def __init__(self, java_unit):
        self._unit = java_unit

    def __javaobj__(self):
        """ Get the underlying java object. """
        return self._unit

    def __repr__(self):
        return self.__str__()

    def __str__(self):
        return str(self._unit)

    def position(self):
        """ Return the position of this unit as a tuple (x, y). """
        return (self._unit.getX(), self._unit.getY())

    def move(x, y):
        """ Start moving to the coordinates (x, y). """
        self._unit.setDestination(x, y)

