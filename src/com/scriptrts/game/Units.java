package com.scriptrts.game;

import java.io.IOException;

import com.scriptrts.core.Main;
import com.scriptrts.util.ResourceManager;

public class Units {
	private static GameObject headquarters;
	
	//FIXME change to initialize method
	static{
		try{
			Sprite[] sprites = ResourceManager.loadSpriteSet("headquarters.sprite", null);
			headquarters = new GameObject(Main.getGame().getPlayer(), sprites, 
					ResourceManager.loadImage("resource/building/headquarters-frontal.png", 200, 200), 
					0, 0, 0, Direction.North, UnitShape.SHAPE_7x7, UnitClass.Building);
		} catch(IOException e){
			System.err.println("Can't load images or sprites.");
		}
	}
	
	public static GameObject headquarters(){
		return headquarters.copy();
	}
}
