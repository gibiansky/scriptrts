package com.scriptrts.game;

import java.io.IOException;

import com.scriptrts.core.Main;
import com.scriptrts.util.ResourceManager;

public class Units {
	private static GameObject headquarters;
	private static GameObject drone;
	
	//FIXME change to initialize method
	static{
		try{
			Sprite[] hqSprites = ResourceManager.loadSpriteSet("headquarters.sprite", null);
			headquarters = new GameObject(Main.getGame().getPlayer(), hqSprites, 
					ResourceManager.loadImage("resource/building/headquarters-frontal.png", 200, 200), 
					0, 0, 0, Direction.North, UnitShape.SHAPE_7x7, UnitClass.Building);
			
			Sprite[] droneSprites = ResourceManager.loadSpriteSet("drone.sprite", null);
			drone = new GameObject(Main.getGame().getPlayer(), droneSprites, 
					ResourceManager.loadImage("resource/unit/spaceship/Art.png", 200, 200), 
					15, 0, 0, Direction.North, UnitShape.SHAPE_1x1, UnitClass.Standard);
		} catch(IOException e){
			System.err.println("Can't load images or sprites.");
		}
	}
	
	public static GameObject headquarters(){
		return headquarters.copy();
	}
	
	public static GameObject drone(){
		return drone.copy();
	}
}
