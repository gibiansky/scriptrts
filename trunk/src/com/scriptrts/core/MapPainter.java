package com.scriptrts.core;

import java.awt.Polygon;
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
    /**
     * Whether or not to draw debug lines on the map. Initialized to false;
     */
    public static boolean DEBUG = false;

    /**
     * Map which this painter is created to draw on the screen
     */
    private Map toPaint;

    /**
     * Array of integers representing the map, where each integer specifies the index of the corresponding texture
     * in the list of texture images.
     */
    private int[][] terrain;

    /**
     * The list of texture images which is used to draw the map on the board; the texture to draw on each tile is dictated
     * by the index stored in the terrain integer array.
     */
    private BufferedImage[] images = new BufferedImage[TerrainType.values().length];

    /**
     * A data array indicating which tiles need to have transparent masks overlayed on them to avoid hard edges on the map.
     * The first two indices are the x and y locations of the tile, and the third index is the type of mask (top corner, top left side, etc.)
     * The contents of the array indicate which texture to overlay, or -1 if no texture is to be overlayed as a mask.
     */
    private int[][][] masking;

    /**
     * The horizontal and vertical size of the drawn tiles 
     */
    private int tileX, tileY;

    /**
     * An array containing all possible masks that might need to be used. The first index is the type of texture, and the second is the mask type.
     */
    private BufferedImage[][] terrainMasks;

    /**
     * Flags that dictate the order that the masks are stored in the mask arrays 
     */
    private static final int MASK_TOP = 0, MASK_BOTTOM = 1, MASK_LEFT = 2, MASK_RIGHT = 3; 
    private static final int MASK_TOP_LEFT = 4, MASK_TOP_RIGHT = 5, MASK_BOTTOM_LEFT = 6, MASK_BOTTOM_RIGHT = 7;

    /**
     * An array which we can loop over to iterate over all mask types
     */
    private static final int[] MASKS = 
    {MASK_TOP, MASK_BOTTOM, MASK_LEFT, MASK_RIGHT, MASK_TOP_LEFT, MASK_TOP_RIGHT, MASK_BOTTOM_LEFT, MASK_BOTTOM_RIGHT};

    /**
     * An array of black and white layer mask images that are applied to textures to create the necessary masks 
     */
    private static BufferedImage[] maskImages;

    /**
     * Constructor which takes a map and an initial tile size, and initializes all necessary elements of the painter
     */
    public MapPainter(Map map, int tileX, int tileY){
        /* Store fields */
        this.toPaint = map;
        this.tileX = tileX;
        this.tileY = tileY;

        /* Load the layer masks used to create the terrain masks */
        try {
            BufferedImage maskTopLeft = ImageIO.read(new File("resource/mask/TileMaskTL.png"));
            BufferedImage maskTopRight = ImageIO.read(new File("resource/mask/TileMaskTR.png"));
            BufferedImage maskBottomLeft = ImageIO.read(new File("resource/mask/TileMaskBL.png"));
            BufferedImage maskBottomRight = ImageIO.read(new File("resource/mask/TileMaskBR.png"));
            BufferedImage maskTop = ImageIO.read(new File("resource/mask/TileMaskTop.png"));
            BufferedImage maskBottom = ImageIO.read(new File("resource/mask/TileMaskBottom.png"));
            BufferedImage maskLeft = ImageIO.read(new File("resource/mask/TileMaskLeft.png"));
            BufferedImage maskRight = ImageIO.read(new File("resource/mask/TileMaskRight.png"));

            maskImages = new BufferedImage[]{maskTop, maskBottom, maskLeft, maskRight, maskTopLeft, maskTopRight, maskBottomLeft, maskBottomRight};
        } catch(IOException e){
            e.printStackTrace();
        }


        /* Load the terrain textures using the texture data file */
        try {
            /* The format of the file we're reading is any number of lines, where the first word is 
             * the name (in the enum) of the tile terrain. The first word is followed by a ", ", followed by
             * the filename of the image texture (sans ".png"). For instance,
             *      Dirt, Martian Rock
             *      Grass, Prarie Grass
             *      Water, Blue Aqua
             *      ... and so on ...
             */
            BufferedReader reader = new BufferedReader(new FileReader("resource/map/textureTrans.dat"));
            String str;
            TerrainType[] values = TerrainType.values();
            while ((str = reader.readLine()) != null) {
                String[] line = str.split(",");
                String tt = line[0].trim();
                String imgname = line[1].trim();

                /* Store the loaded image in the images array at the correct index */
                for(TerrainType t : values)
                    if(t.name().equals(tt)) {
                        images[t.ordinal()] = ImageIO.read(new File("resource/map/" + imgname + ".png"));
                        break;
                    }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Convert terrain to ints instead of enums */
        TerrainType[][] terrainTypes = map.getTileArray();
        terrain = new int[terrainTypes.length][terrainTypes[0].length];
        for(int i = 0; i < terrain.length; i++)
            for(int j = 0; j < terrain[0].length; j++)
                terrain[i][j] = terrainTypes[i][j].ordinal();


        /* Find the number of possible terrains in this map, 
         * so we know how much space to allocate for the masks
         */
        int max = images.length;

        /* Create the masks for each type of terrain */
        terrainMasks = new BufferedImage[max][MASKS.length];
        createTerrainMasks();

        /* Calculate what type of masking is necessary */
        calculateMasking();
    }

    /**
     * Initialize the terrain masks (the half-transparent images that are painted on the edges of tiles to make transitions smoother)
     */
    private void createTerrainMasks(){
        /* For each type of terrain */
        for(int i = 0; i < terrainMasks.length; i++){
            /* Get the terrain image which will be turned into the mask */
            BufferedImage originalImg = images[i];

            /* For each type of mask */
            for(int j = 0; j < terrainMasks[0].length; j++){
                /* Get the black and white image that will be used to create the mask */
                BufferedImage mask = maskImages[j];

                /* Create the mask */
                BufferedImage maskImg = new BufferedImage(originalImg.getWidth(), originalImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for(int pixel_i = 0; pixel_i < originalImg.getWidth(); pixel_i++){
                    for(int pixel_j = 0; pixel_j < originalImg.getHeight(); pixel_j++){
                        /* Get colors of mask image and mask itself */
                        int color = originalImg.getRGB(pixel_i, pixel_j);
                        int maskColor = mask.getRGB(pixel_i, pixel_j);

                        /* Extract the pieces of the color */
                        int alpha = ((maskColor & 0xFF000000) >> 24);
                        int intensity = 255 - ((maskColor & 0x00FF0000) >> 16); 
                        int red = ((color & 0x00FF0000) >> 16); 
                        int green = ((color & 0x0000FF00) >> 8);
                        int blue = ((color & 0x000000FF) >> 0); 

                        /* Black means that the alpha is high, white means the alpha is low */
                        int newColor = (intensity << 24) | (red << 16) | (green << 8) | blue;

                        maskImg.setRGB(pixel_i, pixel_j, alpha == 0 ? color : newColor);
                    }
                }

                /* Store resulting mask in the mask array */
                terrainMasks[i][j] = maskImg;
            }
        }
    }

    /**
     * Calculate which tiles need to have masking on them and what type of masking to apply
     */
    private void calculateMasking() {
        /* Disable masking by default */
        masking = new int[terrain.length][terrain[0].length][8];
        for(int i = 0; i < terrain.length; i++)
            for(int j = 0; j < terrain[0].length; j++)
                for(int k = 0; k < 8; k++)
                    masking[i][j][k] = -1;


        /* Mask each tile separately */
        for(int i = 0; i < terrain.length; i++){
            for(int j = 0; j < terrain[0].length; j ++){
                /* For each tile we proceed to:
                 *      Check the tiles around it;
                 *      If the tile around it has precedence over the tile we're examining,
                 *          then we apply a mask in the direction of the tile.
                 *      Repeat for all directions: 
                 *          up, down, left, right; diagonal top-left, top-right, bottom-left, bottom-right.
                 */

                /* Top right side of the tile */
                int newIndexX = i -1;

                /* We have to change how we get the y index depending on
                 * our x-index because every other row is shifted to the right */
                int newIndexY = (i % 2 == 0 ? j : j + 1);

                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_TOP_RIGHT);

                /* Bottom left side of the tile */
                newIndexX = i + 1;
                newIndexY = (i % 2 == 0 ? j - 1: j);
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_BOTTOM_LEFT);

                /* Mask sides going from the top left to bottom right */
                /* Top left side of the tile */
                newIndexX = i - 1;
                newIndexY = (i % 2 == 0 ? j - 1: j);
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_TOP_LEFT);

                /* Bottom right side of the tile */
                newIndexX = i + 1;
                newIndexY = (i % 2 == 0 ? j : j + 1);
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_BOTTOM_RIGHT);

                /* Top  corner */
                newIndexX = i - 2;
                newIndexY = j;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length){
                    if(terrain[i][j] < terrain[newIndexX][newIndexY]) {
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_TOP);
                    }
                }

                /* Bottom corner */
                newIndexX = i + 2;
                newIndexY = j;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_BOTTOM);

                /* Top  corner */
                newIndexX = i;
                newIndexY = j-1;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j,  terrain[newIndexX][newIndexY], MASK_LEFT);

                /* Bottom corner */
                newIndexX = i;
                newIndexY = j+1;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_RIGHT);
            }
        }
    }

    private boolean isMaskedBottomLeft(int i, int j, int[][] terrain){
        int newIndexX = i + 1;
        int newIndexY = (i % 2 == 0 ? j - 1: j);
        if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
            if(terrain[i][j] < terrain[newIndexX][newIndexY])
                return true;
            
        return false;
    }
    private boolean isMaskedBottomRight(int i, int j, int[][] terrain){
        int newIndexX = i + 1;
        int newIndexY = (i % 2 == 0 ? j : j + 1);
        if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
            if(terrain[i][j] < terrain[newIndexX][newIndexY])
                return true;
            
        return false;
    }

    /**
     * Enable masking for the given tile with a certain type of mask and a certain masking terrain
     */
    private void enableMasking(int i, int j, int maskingTerrain, int maskID){
        masking[i][j][maskID] = maskingTerrain;
    }

    /**
     * Retrieve the mask image for this terrain and this type of mask
     */
    private BufferedImage getMask(int terrain, int maskID){
        return terrainMasks[terrain][maskID];
    }

    /**
     * Signifies that the input map to this map painter was changed, and that the map painter
     * should change its internal state to reflect the change in the map.
     */
    public synchronized void update(){
        TerrainType[][] terrainTypes = toPaint.getTileArray();
        terrain = new int[terrainTypes.length][terrainTypes[0].length];
        for(int i = 0; i < terrain.length; i++)
            for(int j = 0; j < terrain[0].length; j++)
                terrain[i][j] = terrainTypes[i][j].ordinal();

        /* Prevent it from redrawing while masking calculations aren't complete */
        calculateMasking();
    }

    /**
     * Paint the specified part of the map onto the screen using the provided Graphics2D object
     */
    public synchronized void paintMap(Graphics2D graphics, int left, int top, int width, int height){
        /* Calculate the boundaries of the visible map */
        int bottom = top + height;
        int right = left + width;

        /* Only draw what we need to */
        for(int i = top; i < bottom; i++) {
            for(int j = left; j < right; j++) {
                /*
                 * Shift every other row to the right to create the zig-zag pattern going down 
                 * We have to do this because we are using fake isometric perspective.
                 */
                int x;
                if(i % 2 == 0)
                    x = j*tileX;
                else 
                    x = j*tileX + tileX/2;

                /* Draw the tile */
                Image image = images[terrain[i][j]];
                graphics.drawImage(image, x, i*tileY/2, tileX, tileY, null);

                /* Draw the masks on top of the tile */
                for(int texType = 0; texType < images.length; texType++){
                    for(int maskID : MASKS){
                        int maskingTerrain = masking[i][j][maskID];

                        /* If the terrain is less than zero, that means we don't need any masking at all */
                        if(maskingTerrain == texType){
                            BufferedImage mask = getMask(maskingTerrain, maskID);
                            graphics.drawImage(mask, x, i*tileY/2, tileX, tileY, null);
                        }
                    }
                }

                if(MapPainter.DEBUG){
                    graphics.setColor(Color.RED);
                    graphics.drawString("(" + i + ", " + j + ": " + terrain[i][j] + ")",  x + tileX/2 - 40, i*tileY/2 + tileY/2);

                    int[] xpts = new int[]{tileX/2, tileX, tileX/2, 0};
                    int[] ypts = new int[]{0, tileY/2, tileY, tileY/2};
                    Polygon mainTile = new Polygon(xpts, ypts, 4);
                    mainTile.translate(x, i*tileY/2);
                    graphics.drawPolygon(mainTile);
                }
            }
        }
    }

    /**
     * Get an array of polygons that represent the tiles on the board. Used for detecting mouse clicks.
     */
    public Polygon[][] getVisibleTilePolygons(int left, int top, int width, int height){
        /* Calculate the boundaries of the visible map */
        int bottom = top + height;
        int right = left + width;
        
        Polygon[][] polys = new Polygon[(bottom - top)][(right - left)];

        /* Only draw what we need to */
        for(int i = top; i < bottom; i++) {
            for(int j = left; j < right; j++) {
                /*
                 * Shift every other row to the right to create the zig-zag pattern going down 
                 * We have to do this because we are using fake isometric perspective.
                 */
                int x;
                if(i % 2 == 0)
                    x = j*tileX;
                else 
                    x = j*tileX + tileX/2;

                int[] xpts = new int[]{tileX/2, tileX, tileX/2, 0};
                int[] ypts = new int[]{0, tileY/2, tileY, tileY/2};
                Polygon mainTile = new Polygon(xpts, ypts, 4);
                mainTile.translate(x, i*tileY/2);

                polys[i - top][j - left] = mainTile;
            }
        }

        return polys;
    }
}
