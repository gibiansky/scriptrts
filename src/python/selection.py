print "Initializing selection module..."

from com.scriptrts.control import Selection as JSelection
from com.scriptrts.control import SelectionStorage as JSelectionStorage

# Global and player selections
def current(new_selection = None):
    if new_selection == None:
        return Selection(JSelection.current())
    else
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
            self._list.append(element)
            
        self._iter = self._list.__iter__()

    def next(self, default=None):
        return self._iter.next()

class Selection:
    def __init__(self, java_selection):
        self._selection = java_selection

    def __iter__(self):
        return SelectionIterator(self._selection.getList())

    def remove(self, obj):
        self._selection.remove(obj)
    
