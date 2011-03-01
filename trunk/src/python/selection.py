from com.scriptrts.control import Selection as JSelection
from com.scriptrts.control import SelectionStorage as JSelectionStorage
from lib import *

# Global and player selections
def current(new_selection = None):
    if new_selection == None:
        return Selection(JSelection.current())
    else:
        JSelection.current().clear()
        for unit in new_selection:
            JSelection.current().add(unit)
def enemy():
    return Selection(JSelection.enemy())
def ally():
    return Selection(JSelection.ally())
def terrain():
    return Selection(JSelection.terrain())
def all():
    return Selection(JSelection.all())

def group(number, new_group = None):
    if new_group == None:
        return Selection(JSelectionStorage.retrieve(int(number)))
    else:
        new_sel = JSelection()
        for x in new_group:
            new_sel.add(x)
        JSelectionStorage.store(new_sel, number)

class SelectionIterator:
    def __init__(self, java_list):
        self._list = []
        for element in java_list:
            self._list.append(Unit(element))
            
        self._iter = self._list.__iter__()

    def next(self, default=None):
        return self._iter.next()

class Selection:
    def __init__(self, java_selection):
        self._selection = java_selection

    def __iter__(self):
        return SelectionIterator(self._selection.getList())

    def __javaobj__(self):
        return self._selection

    def remove(self, obj):
        self._selection.remove(obj.__javaobj__())

    def add(self, obj):
        if type(obj) is list or isintance(obj, Selection):
            for o in obj:
                self.add(o)
        elif not obj is None:
            self._selection.add(obj.__javaobj__())

    def __iadd__(self, other_sel):
        new_sel = Selection()
        for s in self:
            new_sel.add(s)
        for s in other_sel:
            new_sel.add(s)
        return new_sel

    def __getitem__(self, ind):
        if ind >= 0 && ind < len(self):
            return Unit(self._selection.getList().get(ind))

    def __setitem__(self, ind, u):
        if not u is None and ind >= 0 && ind < len(self):
            self._selection.getList().set(ind, u)

    def __delitem__(self, ind):
        if ind >= 0 && ind < len(self):
            self._selection.getList().remove(ind)

    def __repr__(self):
        return self.__str__()

    def __len__(self):
        return self._selection.getList().size()

    def __str__(self):
        st = '['
        for unit in self:
            st += str(unit) + ', '
        if len(self) > 0:
            st = st[:-2]
        st += ']'
        return st

    
