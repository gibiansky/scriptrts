from com.scriptrts.control import Selection as JSelection
from com.scriptrts.control import SelectionStorage as JSelectionStorage
from lib import *

# Global selections
def current(new_selection = None):
    """ Return the currently selected units or set the current selection. """
    if new_selection == None:
        return Selection(JSelection.current())
    else:
        JSelection.current().clear()
        for unit in new_selection:
            JSelection.current().add(unit)
def enemy():
    """ Return the enemy units. """
    return Selection(JSelection.enemy())
def ally():
    """ Return the allied units. """
    return Selection(JSelection.ally())
def terrain():
    """ Return the terrain features. """
    return Selection(JSelection.terrain())
def all():
    """ Return all terrain features and units visible. """
    return Selection(JSelection.all())

# Ctrl-<Number> groups
def group(number, new_group = None):
    """ Return the Control group or set the control group at a certain index. """
    if new_group == None:
        return Selection(JSelectionStorage.retrieve(int(number)))
    else:
        new_sel = JSelection()
        for x in new_group:
            new_sel.add(x)
        JSelectionStorage.store(new_sel, number)

# Iterator object to loop over selections
class SelectionIterator:
    def __init__(self, java_list):
        # Convert the selection into a list
        self._list = []
        for element in java_list:
            self._list.append(Unit(element))
            
        # Store an iterator for the list
        self._iter = self._list.__iter__()

    # When iterating, just use the next value of the list iterator
    def next(self, default=None):
        return self._iter.next()

# A grouping of units, a selection
class Selection:
    def __init__(self, java_selection):
        """ Create a new selection """
        self._selection = java_selection

    def __iter__(self):
        """ Get an iterator for a selection """
        return SelectionIterator(self._selection.getList())

    def __javaobj__(self):
        """ Get the underlying java object """
        return self._selection

    def remove(self, obj):
        """ Remove a unit from a selection """
        self._selection.remove(obj.__javaobj__())

    
    def add(self, obj):
        """
        Add units to a selection.

        If the passed in object is a unit, the unit is added.
        If the passed in object is a list or selection, all units in the list or selection are added.
        """
        if type(obj) is list or isinstance(obj, Selection):
            for o in obj:
                self.add(o)
        elif not obj is None:
            self._selection.add(obj.__javaobj__())

    def __iadd__(self, other_sel):
        """ Combine two selections into a new selection """
        new_sel = Selection()
        for s in self:
            new_sel.add(s)
        for s in other_sel:
            new_sel.add(s)
        return new_sel

    def __getitem__(self, ind):
        """ Get a unit out of a selection """
        if ind >= 0 and ind < len(self):
            return Unit(self._selection.getList().get(ind))

    def __setitem__(self, ind, u):
        """ Set a unit in a selection """
        if not u is None and ind >= 0 and ind < len(self):
            self._selection.getList().set(ind, u)

    def __delitem__(self, ind):
        """ Remove a unit from a selection """
        if ind >= 0 and ind < len(self):
            self._selection.getList().remove(ind)

    def __repr__(self):
        """ Convert this selection to a string """
        return self.__str__()

    def __len__(self):
        """ Return the number of units in this selection """
        return self._selection.getList().size()

    def __str__(self):
        """ Print this selection as if it were a list of units """
        st = '['
        for unit in self:
            st += str(unit) + ', '
        if len(self) > 0:
            st = st[:-2]
        st += ']'
        return st

    
