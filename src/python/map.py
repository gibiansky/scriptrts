from com.scriptrts.core import Main as JMain
from com.scriptrts.core import Map as JMap
from com.scriptrts.core import TerrainType as JTerrainType
from com.scriptrts.game import UnitGrid as JUnitGrid
from lib import *

# Map object
_map = JMain.getCurrentMap()

# Terrian types may be accessed through map.Dirt, map.Grass, etc
for ttype in JTerrainType.values():
    locals()[ttype.name()] = ttype

# Get the terrain type at a certain location
def tiletype(x, y):
    tiles = _map.getTileArray()
    return tiles[x][y]

# Get the unit at a certain location, or None if there is no unit there
def unit(x, y):
    unitgrid = JMain.getUnitGrid()
    if unitgrid.getUnit(x, y):
        return Unit(unitgrid.getUnit(x, y))
    else:
        return None
