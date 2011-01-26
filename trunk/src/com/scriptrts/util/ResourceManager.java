package com.scriptrts.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.scriptrts.core.SpriteType;
import com.scriptrts.core.TerrainType;

public class ResourceManager {

	private static ResourceManager manager = new ResourceManager();
	
	private HashMap<TerrainType, BufferedImage> tiles;
	private HashMap<SpriteType, BufferedImage> sprites;
	
	/**
	 * Loads textures and sprites
	 * @return whether the textures have been loaded successfully
	 */
	public boolean loadTiles(int tileWidth, int tileHeight) {
		/* Load necessary textures */
		System.out.println("Loading textures");
		/* Set up correspondences between TerrainTypes and their images */
		try {
			BufferedReader reader = new BufferedReader(new FileReader("resource/map/textureTrans.dat"));
			String str;
			TerrainType[] values = TerrainType.values();
		    while ((str = reader.readLine()) != null) {
		        String[] line = str.split(",");
		        String tt = line[0].trim();
		        String imgname = line[1].trim();
		        for(TerrainType t : values)
		        	if(t.name().equals(tt)) {
		        		BufferedImage temp = ImageIO.read(new File("resource/map/" + imgname + ".png"));
		        		BufferedImage tileImg = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_INT_ARGB);
		        		Graphics2D g2 = (Graphics2D) tileImg.getGraphics();
		        		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		        		g2.drawImage(temp, 0, 0, tileWidth, tileHeight, null);
		        		g2.dispose();
		        		tiles.put(t, tileImg);
		        		break;
		        	}
		    }
		    reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Retrieves the image for the requested terrain tile
	 * @param type the terrain type for the tile
	 * @return the image of the given terrain type
	 */
	public BufferedImage getTileImage(TerrainType type) {
		return tiles.get(type);
	}
	
	/**
	 * Retrieves the image for the requested unit
	 * @param type the unit
	 * @return the sprite for the unit
	 */
	public BufferedImage getSpriteImage(SpriteType type) {
		return sprites.get(type);
	}
	
	public static ResourceManager getResourceManager() {
		return manager;
	}
}
