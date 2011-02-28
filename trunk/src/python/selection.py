print "Initializing selection module..."

from com.scriptrts.control import Selection as JSelection

# Global and player selections
def current():
    return Selection(JSelection.current())
def enemy():
    return Selection(JSelection.enemy())
def ally():
    return Selection(JSelection.ally())
def terrain():
    return Selection(JSelection.terrain())
def all():
    return Selection(JSelection.all())

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
    
