package com.scriptrts.core;

import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.scriptrts.core.ui.UnitPainter;
import com.scriptrts.core.ui.Viewport;
import com.scriptrts.core.ClickAction;
import com.scriptrts.game.GameObject;

public class PlaceAction extends ClickAction {
    private GameObject unit = null;

    public PlaceAction(GameObject placing){
        super();
        unit = placing;
    }

    public void click(int x, int y){
        Viewport viewport = Main.getGame().getViewport();
        Point point = new Point(x, y);
        Point unitTile = Main.getGame().getUnitPainter().unitTileAtPoint(point, viewport);
        unit.getUnit().setX(unitTile.x);
        unit.getUnit().setY(unitTile.y);
        Main.getGame().getGameGrid().placeUnit(unit, unitTile.x, unitTile.y);
        Main.getGame().getGameManager().addUnit(unit);
        Main.getGame().getGameManager().setVisibleTiles(unit, unit.getUnit().getX(), unit.getUnit().getY());

        if(Main.getGameClient() != null)
            Main.getGameClient().sendNewUnitNotification(unit);
    }

    public boolean hasCursor() {
        return true;
    }

    public void paintCursor(Graphics graphics, Viewport viewport){
        Main.getGame().getUnitPainter().paintTemporaryUnit((Graphics2D) graphics, viewport, unit, 0, 0);
    }

}
