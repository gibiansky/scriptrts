package com.scriptrts.core;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class MapPainter {

	Map toPaint;
	HashMap<TerrainType, BufferedImage> images;

	 public MapPainter(Map map){
	    	this.toPaint = map;
	    	images = new HashMap<TerrainType, BufferedImage>();
	    	
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
			        		images.put(t, ImageIO.read(new File("resource/map/" + imgname + ".png")));
			        		break;
			        	}
			    }
			    reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	
	
	//    int[][] terrain;
	//    int[][] isMasked;
	//    static boolean masking = false;
	//    ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

	/* Implements a basic map to make sure masked images aren't duplicated */
	/* The point array stores pairs of integers: the first integer is the tile that is the original, 
	 * the second integer is the tile that is the masking tile.
	 * The integer array stores the result of the mask, i.e. the index of the image that uses the mask.
	 */
	//    ArrayList<Point> maskTypes = new ArrayList<Point>();
	//    ArrayList<Integer> imageIndices = new ArrayList<Integer>();

	//    static BufferedImage maskTopLeft, maskTopRight, maskBottomLeft, maskBottomRight;
	//    static {
	//        try {
	//            maskTopLeft = ImageIO.read(new File("resource/mask/TileMaskTL.png"));
	//            maskTopRight = ImageIO.read(new File("resource/mask/TileMaskTR.png"));
	//            maskBottomLeft = ImageIO.read(new File("resource/mask/TileMaskBL.png"));
	//            maskBottomRight = ImageIO.read(new File("resource/mask/TileMaskBR.png"));
	//        } catch(IOException e){
	//            e.printStackTrace();
	//        }
	//    }

	//    public MapPainter(int[][] mapTerrain, BufferedImage[] tileImages){
	//        /* Add all tile images to our image cache */
	//        for(BufferedImage img : tileImages)
	//            images.add(img);
	//
	//        /* Copy over the terrain into our terrain array */
	//        terrain = new int[mapTerrain.length][mapTerrain[0].length];
	//        isMasked = new int[mapTerrain.length][mapTerrain[0].length];
	//        for(int i = 0; i < mapTerrain.length; i++){
	//            for(int j = 0; j < mapTerrain[0].length; j++){
	//                terrain[i][j] = mapTerrain[i][j];
	//            }
	//        }
	//
	//        if(masking){
	//            /* Create new terrain tiles for where terrains merge using masks */
	//            for(int i = 0; i < terrain.length; i++){
	//                for(int j = 0; j < terrain[0].length; j += 2){
	//                    /* Check for top left mask */
	//                    if(i != 0 && j != 0 && terrain[i][j] != terrain[i-1][j-1]) {
	//                        terrain[i][j] = mask(terrain[i][j], terrain[i-1][j-1], maskTopLeft);
	//                    }
	//
	//                    /* Check for top right mask */
	//                    if(i != 0 && j != terrain[0].length - 1 && terrain[i][j] != terrain[i-1][j+1]){
	//                        terrain[i][j] = mask(terrain[i][j], terrain[i-1][j+1], maskTopRight);
	//                    }
	//
	//                    /* Check for bottom left mask */
	//                    if(i != terrain.length - 1 && j != 0 && terrain[i][j] != terrain[i+1][j-1]) {
	//                        terrain[i][j] = mask(terrain[i][j], terrain[i+1][j-1], maskBottomLeft);
	//                    }
	//
	//                    /* Check for bottom right mask */
	//                    if(i != terrain.length - 1 && j != terrain[0].length - 1 && terrain[i][j] != terrain[i+1][j+1]){
	//                        terrain[i][j] = mask(terrain[i][j], terrain[i+1][j+1], maskBottomRight);
	//                    }
	//                }
	//            }
	//        }
	//    }

	//    private int mask(int originalType, int maskType, BufferedImage mask){
	//        /* Check that we haven't made this mask before */
	//        for(int i = 0; i < maskTypes.size(); i++){
	//            Point pairing = maskTypes.get(i);
	//            if(pairing.x == originalType && pairing.y == maskType)
	//                return imageIndices.get(i);
	//        }
	//        System.out.println("MAKS " + originalType + " " + maskType);
	//
	//
	//        BufferedImage originalImg = images.get(originalType);
	//        BufferedImage maskImg = images.get(maskType);
	//
	//        BufferedImage result = new BufferedImage(originalImg.getWidth(), originalImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
	//        Graphics drawer = result.getGraphics();
	//
	//        /* Create image by combining mask image and mask, then we will draw this image onto the original to create the result */
	//        BufferedImage layerMask = new BufferedImage(originalImg.getWidth(), originalImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
	//        for(int i = 0; i < originalImg.getWidth(); i++){
	//            for(int j = 0; j < originalImg.getHeight(); j++){
	//                /* Get colors of mask image and mask itself */
	//                int color = maskImg.getRGB(i, j);
	//                int maskColor = mask.getRGB(i, j);
	//
	//                int alpha = ((maskColor & 0xFF000000) >> 24);
	//                int red = ((color & 0x00FF0000) >> 16); 
	//                int green = ((color & 0x0000FF00) >> 8);
	//                int blue = ((color & 0x000000FF) >> 0); 
	//
	//                int newColor = (alpha << 24) | (red << 16) | (green << 8) | blue;
	//                layerMask.setRGB(i, j, newColor);
	//            }
	//        }
	//
	//        drawer.drawImage(originalImg, 0, 0, null);
	//        drawer.drawImage(layerMask, 0, 0, null);
	//
	//        /* return id of new thing */
	//        int newImgIndex = images.size() - 1;
	//        images.add(result);
	//
	//        /* before returning, add this to the cache */
	//        maskTypes.add(new Point(originalType, maskType));
	//        imageIndices.add(newImgIndex);
	//
	//        return newImgIndex;
	//    }

	public void paintMap(Graphics2D graphics, int left, int top, int width, int height, int tileX, int tileY){
		int bottom = top + height;
		int right = left + width;

		for(int i = top; i < bottom; i++) {
			for(int j = left; j < right; j++) {
				/* Draw the tile */
				int x;
				if(i % 2 == 0)
					x = j*tileX;
				else 
					x = j*tileX + tileX/2;

				//                Image image = images.get(terrain[i][j]);
				Image image = images.get(toPaint.getTileArray()[i][j]);
				graphics.drawImage(image, x, i*tileY/2, tileX, tileY, null);
			}
		}
	}

}
