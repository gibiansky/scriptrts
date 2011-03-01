class Unit:
    def __init__(self, java_unit):
        self._unit = java_unit

    def __javaobj__(self):
        return self._unit

    def __repr__(self):
        return self.__str__()

    def __str__(self):
        return str(self._unit)

    def position(self):
        return (self._unit.getX(), self._unit.getY())

from com.scriptrts.game import Direction as JDirection
class direction: pass
for direct in JDirection.values():
    direction.__dict__[direct.name()] = direct
