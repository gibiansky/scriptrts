package com.scriptrts.core;

import javax.swing.*;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
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

    private Map toPaint;
    private int[][] terrain;
    private int[][] originalTerrain;
    private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>(TerrainType.values().length);
    private int[][] masking;
    private int tileX, tileY;

    private ArrayList<MaskRequest> maskTypes = new ArrayList<MaskRequest>();
    private ArrayList<Integer> imageIndices = new ArrayList<Integer>();

    /* Side masks */
    static BufferedImage maskTopLeft, maskTopRight, maskBottomLeft, maskBottomRight;

    /* Corner masks */
    static BufferedImage maskTop, maskBottom, maskLeft, maskRight;

    public MapPainter(Map map, int tileX, int tileY){
        this.toPaint = map;
        TerrainType[][] terrainTypes = map.getTileArray();
        this.tileX = tileX;
        this.tileY = tileY;

        /* Grow the images list to the right size */
        for(int i = 0; i < TerrainType.values().length; i++)
            images.add(null);

        /* Load masks */
        try {
            maskTopLeft = resizeImage(ImageIO.read(new File("resource/mask/TileMaskTL.png")), tileX, tileY);
            maskTopRight = resizeImage(ImageIO.read(new File("resource/mask/TileMaskTR.png")), tileX, tileY);
            maskBottomLeft = resizeImage(ImageIO.read(new File("resource/mask/TileMaskBL.png")), tileX, tileY);
            maskBottomRight = resizeImage(ImageIO.read(new File("resource/mask/TileMaskBR.png")), tileX, tileY);
            maskTop = resizeImage(ImageIO.read(new File("resource/mask/TileMaskTop.png")), tileX, tileY);
            maskBottom = resizeImage(ImageIO.read(new File("resource/mask/TileMaskBottom.png")), tileX, tileY);
            maskLeft = resizeImage(ImageIO.read(new File("resource/mask/TileMaskLeft.png")), tileX, tileY);
            maskRight = resizeImage(ImageIO.read(new File("resource/mask/TileMaskRight.png")), tileX, tileY);
        } catch(IOException e){
            e.printStackTrace();
        }


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
                        images.set(t.ordinal(), resizeImage(ImageIO.read(new File("resource/map/" + imgname + ".png")), tileX, tileY));
                        break;
                    }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Convert terrain to ints */
        terrain = new int[terrainTypes.length][terrainTypes[0].length];
        for(int i = 0; i < terrain.length; i++)
            for(int j = 0; j < terrain[0].length; j++)
                terrain[i][j] = terrainTypes[i][j].ordinal();

        originalTerrain = new int[terrainTypes.length][terrainTypes[0].length];
        for(int i = 0; i < terrain.length; i++)
            for(int j = 0; j < terrain[0].length; j++)
                originalTerrain[i][j] = terrain[i][j];

        /* Calculate what type of masking is necessary */
        masking = new int[terrain.length][terrain[0].length];
        calculateMasking();
    }

    private BufferedImage resizeImage(BufferedImage img, int newSizeX, int newSizeY){
        BufferedImage result = new BufferedImage(newSizeX, newSizeY, BufferedImage.TYPE_INT_ARGB);
        result.getGraphics().drawImage(img, 0, 0, newSizeX, newSizeY, null);
        return result;
    }

    private class MaskRequest {
        int original, mask;
        BufferedImage img;

        public MaskRequest(int o, int m, BufferedImage i){
            super();
            original = o;
            mask = m;
            img = i;
        }
    }

    private void calculateMasking() {
        /* Mask sides going from the top right to bottom left */
        ArrayList<MaskRequest> maskRequestsForTile = new ArrayList<MaskRequest>(8);
        for(int i = 0; i < terrain.length; i++){
            for(int j = 0; j < terrain[0].length; j ++){
                /* Get rid of previous requests */
                maskRequestsForTile.clear();

                /* Top right side of the tile */
                int newIndexX = i -1;
                int newIndexY = (i % 2 == 0 ? j : j + 1);
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length){
                    if(originalTerrain[i][j] != originalTerrain[newIndexX][newIndexY]){
                        if(originalTerrain[i][j] > originalTerrain[newIndexX][newIndexY])
                            maskRequestsForTile.add(new MaskRequest(terrain[i][j], originalTerrain[newIndexX][newIndexY], maskTopRight));
                    }
                }

                /* Bottom left side of the tile */
                newIndexX = i + 1;
                newIndexY = (i % 2 == 0 ? j - 1: j);
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length){
                    if(originalTerrain[i][j] != originalTerrain[newIndexX][newIndexY]){
                        if(originalTerrain[i][j] > originalTerrain[newIndexX][newIndexY])
                            maskRequestsForTile.add(new MaskRequest(terrain[i][j], originalTerrain[newIndexX][newIndexY], maskBottomLeft));
                    }
                }

                /* Mask sides going from the top left to bottom right */
                /* Top left side of the tile */
                newIndexX = i - 1;
                newIndexY = (i % 2 == 0 ? j - 1: j);
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length){
                    if(originalTerrain[i][j] != originalTerrain[newIndexX][newIndexY]){
                        if(originalTerrain[i][j] > originalTerrain[newIndexX][newIndexY])
                            maskRequestsForTile.add(new MaskRequest(terrain[i][j], originalTerrain[newIndexX][newIndexY], maskTopLeft));
                    }
                }

                /* Bottom right side of the tile */
                newIndexX = i + 1;
                newIndexY = (i % 2 == 0 ? j : j + 1);
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length){
                    if(originalTerrain[i][j] != originalTerrain[newIndexX][newIndexY]){
                        if(originalTerrain[i][j] > originalTerrain[newIndexX][newIndexY])
                            maskRequestsForTile.add(new MaskRequest(terrain[i][j], originalTerrain[newIndexX][newIndexY], maskBottomRight));
                    }
                }

                /* Top  corner */
                newIndexX = i - 2;
                newIndexY = j;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length){
                    if(originalTerrain[i][j] != originalTerrain[newIndexX][newIndexY]){
                        if(originalTerrain[i][j] > originalTerrain[newIndexX][newIndexY])
                            maskRequestsForTile.add(new MaskRequest(terrain[i][j], originalTerrain[newIndexX][newIndexY], maskTop));
                    }
                }

                /* Bottom corner */
                newIndexX = i + 2;
                newIndexY = j;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length){
                    if(originalTerrain[i][j] != originalTerrain[newIndexX][newIndexY]){
                        if(originalTerrain[i][j] > originalTerrain[newIndexX][newIndexY])
                            maskRequestsForTile.add(new MaskRequest(terrain[i][j], originalTerrain[newIndexX][newIndexY], maskBottom));
                    }
                }

                /* Top  corner */
                newIndexX = i;
                newIndexY = j-1;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length){
                    if(originalTerrain[i][j] != originalTerrain[newIndexX][newIndexY]){
                        if(originalTerrain[i][j] > originalTerrain[newIndexX][newIndexY])
                            maskRequestsForTile.add(new MaskRequest(terrain[i][j], originalTerrain[newIndexX][newIndexY], maskLeft));
                    }
                }

                /* Bottom corner */
                newIndexX = i;
                newIndexY = j+1;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length){
                    if(originalTerrain[i][j] != originalTerrain[newIndexX][newIndexY]){
                        if(originalTerrain[i][j] > originalTerrain[newIndexX][newIndexY])
                            maskRequestsForTile.add(new MaskRequest(terrain[i][j], originalTerrain[newIndexX][newIndexY], maskRight));
                    }
                }

                terrain[i][j] = combinedMask(maskRequestsForTile, terrain[i][j]);
            }
        }
    }

    private int combinedMask(ArrayList<MaskRequest> requests, int terrain){
        if(requests.isEmpty()) 
            return terrain;

        for(int i = 0; i < requests.size(); i++)
            terrain = performMask(requests.get(i), terrain);
        return terrain;
    }

    private int performMask(MaskRequest req, int originalType){
        int maskType = req.mask;
        BufferedImage mask = req.img;
        for(int i = 0; i < maskTypes.size(); i++)
            if(maskTypes.get(i).original == originalType && maskTypes.get(i).mask == maskType && maskTypes.get(i).img == mask)
                return imageIndices.get(i);

        BufferedImage originalImg = images.get(originalType);
        BufferedImage maskImg = images.get(maskType);

        final BufferedImage result = new BufferedImage(originalImg.getWidth(), originalImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics drawer = result.getGraphics();

        /* Create image by combining mask image and mask, then we will draw this image onto the original to create the result */
        BufferedImage layerMask = new BufferedImage(originalImg.getWidth(), originalImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for(int i = 0; i < originalImg.getWidth(); i++){
            for(int j = 0; j < originalImg.getHeight(); j++){
                /* Get colors of mask image and mask itself */
                int color = maskImg.getRGB(i, j);
                int maskColor = mask.getRGB(i, j);

                int alpha = ((maskColor & 0xFF000000) >> 24);
                int intensity = 255 - ((maskColor & 0x00FF0000) >> 16); 
                int red = ((color & 0x00FF0000) >> 16); 
                int green = ((color & 0x0000FF00) >> 8);
                int blue = ((color & 0x000000FF) >> 0); 

                int newColor = (intensity << 24) | (red << 16) | (green << 8) | blue;
                layerMask.setRGB(i, j, alpha == 0 ? color : newColor);
            }
        }

        drawer.drawImage(originalImg, 0, 0, null);
        drawer.drawImage(layerMask, 0, 0, null);

        /* return id of new thing */
        int newImgIndex = images.size();
        images.add(result);

        /* before returning, add this to the cache */
        MaskRequest mr = new MaskRequest(originalType, maskType, mask);
        maskTypes.add(mr);
        imageIndices.add(newImgIndex);

        return newImgIndex;
    }

    public void paintMap(Graphics2D graphics, int left, int top, int width, int height){
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

                Image image = images.get(terrain[i][j]);
                graphics.drawImage(image, x, i*tileY/2, tileX, tileY, null);
                graphics.setColor(Color.RED);
            }
        }
    }

}
