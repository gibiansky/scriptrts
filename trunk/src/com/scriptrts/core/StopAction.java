package com.scriptrts.core;

import java.awt.Point;

import com.scriptrts.control.Selection;
import com.scriptrts.control.StopOrder;
import com.scriptrts.game.GameObject;

public class StopAction extends ClickAction {

	@Override
	public void click(int x, int y) {
		/*
		 * If Shift is held down, queue the order to move; else override.
		 */
		for(GameObject s : Selection.current().getList())
			s.getUnit().getOrderHandler().order(new StopOrder());
	}

}
