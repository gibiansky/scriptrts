package com.scriptrts.core;

import java.awt.Point;
import java.awt.event.KeyEvent;

import com.scriptrts.control.MoveOrder;
import com.scriptrts.control.Selection;
import com.scriptrts.core.ui.InputManager;
import com.scriptrts.game.GameObject;

public class MoveAction extends ClickAction {

	@Override
	public boolean click(int x, int y) {
		Point unitTile = Main.getGame().getUnitPainter().unitTileAtPoint(new Point(x,y), Main.getGame().getViewport());
		/*
		 * If Shift is held down, queue the order to move; else override.
		 */
		if(InputManager.getInputManager().getKeyCodeFlag(KeyEvent.VK_SHIFT))
				for(GameObject s : Selection.current().getList())
					s.getUnit().getOrderHandler().queueOrder(new MoveOrder(unitTile));
		else
			for(GameObject s : Selection.current().getList())
				s.getUnit().getOrderHandler().order(new MoveOrder(unitTile));
		return true;
	}

}
