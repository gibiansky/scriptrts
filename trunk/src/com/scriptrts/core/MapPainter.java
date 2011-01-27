package com.scriptrts.core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import com.scriptrts.util.ResourceManager;

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
    private BufferedImage[] scaledImages = new BufferedImage[TerrainType.values().length];

    /**
     * A data array indicating which tiles need to have transparent masks overlayed on them to avoid hard edges on the map.
     * The first two indices are the x and y locations of the tile, and the third index is the type of mask (top corner, top left side, etc.)
     * The contents of the array indicate which texture to overlay, or -1 if no texture is to be overlayed as a mask.
     */
    private int[][][] masking;

    /**
     * The horizontal and vertical size of the drawn tiles 
     */
    private int tileX = 128, tileY = 64;

    /**
     * The maximum size for the tiles
     */
    public static final int MAX_TILE_X = 128, MAX_TILE_Y = 64;

    /**
     * The minimum size for the tiles
     */
    public static final int MIN_TILE_X = 16, MIN_TILE_Y = 8;

    /**
     * An array containing all possible masks that might need to be used. The first index is the type of texture, and the second is the mask type.
     */
    private BufferedImage[][] terrainMasks;
    private BufferedImage[][] scaledTerrainMasks;

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
            BufferedImage maskTopLeft = ResourceManager.loadImage("resource/mask/TileMaskTL.png", MAX_TILE_X, MAX_TILE_Y);
            BufferedImage maskTopRight = ResourceManager.loadImage("resource/mask/TileMaskTR.png", MAX_TILE_X, MAX_TILE_Y);
            BufferedImage maskBottomLeft = ResourceManager.loadImage("resource/mask/TileMaskBL.png", MAX_TILE_X, MAX_TILE_Y);
            BufferedImage maskBottomRight = ResourceManager.loadImage("resource/mask/TileMaskBR.png", MAX_TILE_X, MAX_TILE_Y);
            BufferedImage maskTop = ResourceManager.loadImage("resource/mask/TileMaskTop.png", MAX_TILE_X, MAX_TILE_Y);
            BufferedImage maskBottom = ResourceManager.loadImage("resource/mask/TileMaskBottom.png", MAX_TILE_X, MAX_TILE_Y);
            BufferedImage maskLeft = ResourceManager.loadImage("resource/mask/TileMaskLeft.png", MAX_TILE_X, MAX_TILE_Y);
            BufferedImage maskRight = ResourceManager.loadImage("resource/mask/TileMaskRight.png", MAX_TILE_X, MAX_TILE_Y);

            maskImages = new BufferedImage[]{maskTop, maskBottom, maskLeft, maskRight, maskTopLeft, maskTopRight, maskBottomLeft, maskBottomRight};


            /* Load all images for textures */
            HashMap<String, String> associations = ResourceManager.readAssociationFile("resource/map/textureTrans.dat");
            TerrainType[] values = TerrainType.values();

            /* Store the loaded image in the images array at the correct index */
            for(TerrainType t : values) {
                images[t.ordinal()] = ResourceManager.loadImage("resource/map/" + associations.get(t.name()) + ".png", MAX_TILE_X, MAX_TILE_Y);
                scaledImages[t.ordinal()] = images[t.ordinal()];
            }
        } catch(IOException e){
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
        scaledTerrainMasks = new BufferedImage[max][MASKS.length];
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
                scaledTerrainMasks[i][j] = maskImg;
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
        return scaledTerrainMasks[terrain][maskID];
    }

    /**
     * Attempt to set the tile size to use for this map painter
     * @return true if tile size was set; 
     * false if provided tile size was invalid.
     */
    public synchronized boolean setTileSize(int tileSizeX, int tileSizeY){
        boolean different = (tileSizeX != tileX || tileSizeY != tileY);
        if(tileSizeX <= MapPainter.MAX_TILE_X && tileSizeY <= MapPainter.MAX_TILE_Y) 
            if(tileSizeX >= MapPainter.MIN_TILE_X && tileSizeY >= MapPainter.MIN_TILE_Y) {
                tileX = tileSizeX;
                tileY = tileSizeY;

                if(different)
                    scaleImagesInBackground();
                return true;
            }

        return false;
    }

    /**
     * Creates a background thread to rescale all the images
     */
    private synchronized void scaleImagesInBackground(){
        /* Set all images to be full size until we rescale */
        for(int i = 0; i < images.length; i++)
            scaledImages[i] = images[i];

        for(int i = 0; i < terrainMasks.length; i++)
            for(int j = 0; j < terrainMasks[0].length; j++)
                scaledTerrainMasks[i][j] = terrainMasks[i][j];

        /* Resize in background */
        Thread scaler = new Thread(){
            public void run(){
                for(int i = 0; i < images.length; i++)
                    scaledImages[i] = resizeImage(images[i], tileX, tileY);

                for(int i = 0; i < terrainMasks.length; i++)
                    for(int j = 0; j < terrainMasks[0].length; j++)
                        scaledTerrainMasks[i][j] = resizeImage(terrainMasks[i][j], tileX, tileY);
            }
        };
        scaler.setPriority(Thread.MIN_PRIORITY);
        scaler.start();
    }

    /**
     * Resizes an image to be of desired size
     */
    private BufferedImage resizeImage(Image img, int width, int height){
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) scaledImage.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, width, height, null);
        g.dispose();
        return scaledImage;
    }

    /**
     * Get the width of the tiles
     */
    public int getTileWidth(){
        return tileX;
    }

    /**
     * Get the height of the tiles
     */
    public int getTileHeight(){
        return tileY;
    }

    /**
     * Get the map this painter draws
     */
    public Map getMap(){
        return toPaint;
    }

    /**
     * Get the tile that is at a given point on the map
     */
    public Point getTileAtPoint(Point point, Viewport viewport){
        /* Calculate boundaries of what the viewport sees */
        int left = (int) (viewport.getX() / tileX) - 1;
        int top = (int) (viewport.getY() / (tileY / 2)) - 1;
        if(left < 0) left = 0;
        if(top < 0) top = 0;

        int right = (int) ((viewport.getX() + viewport.getWidth()) / tileX) + 1;
        int bottom = (int) ((viewport.getY() + viewport.getHeight()) / (tileY / 2)) + 1;
        if(right > toPaint.getN()) right = toPaint.getN();
        if(bottom > toPaint.getN()) bottom = toPaint.getN();

        /* Translate the point to be on the map coordinates instead of in screen coordinates */
        point.translate(viewport.getX(), viewport.getY());

        /* Loop through visible tiles */
        for(int i = top; i < bottom; i++) {
            for(int j = left; j < right; j++) {
                /*
                 * Shift every other row to the right to create the zig-zag pattern going down 
                 * We have to do this because we are using fake isometric perspective.
                 */
                int x;
                if(i % 2 == 0){
                    x = j*tileX;
                } else {
                    x = j*tileX + tileX/2;
                }

                int[] xpts = {
                    tileX/2, tileX, tileX/2, 0
                };
                int[] ypts = {
                    0, tileY/2, tileY, tileY/2
                };
                Polygon mainTile = new Polygon(xpts, ypts, 4);
                mainTile.translate(x, i*tileY/2);

                if(mainTile.contains(point))
                    return new Point(j, i);
            }
        }

        return null;
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
    public synchronized void paintMap(Graphics2D graphics, Viewport viewport){
        /* Calculate boundaries of what the viewport sees */
        int left = (int) (viewport.getX() / tileX) - 1;
        int top = (int) (viewport.getY() / (tileY / 2)) - 1;
        if(left < 0) left = 0;
        if(top < 0) top = 0;

        int right = (int) ((viewport.getX() + viewport.getWidth()) / tileX) + 1;
        int bottom = (int) ((viewport.getY() + viewport.getHeight()) / (tileY / 2)) + 1;
        if(right > toPaint.getN()) right = toPaint.getN();
        if(bottom > toPaint.getN()) bottom = toPaint.getN();

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
                Image image = scaledImages[terrain[i][j]];
                graphics.drawImage(image, x, i*tileY/2, tileX, tileY, null);


                /* Don't mask above a certain zoom level */
                if(tileX > 16 && tileY > 8)
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

                /* Draw debug lines and labels */
                if(MapPainter.DEBUG){
                    graphics.setColor(Color.RED);
                    graphics.drawString("(" + i + ", " + j + ": " + terrain[i][j] + ")",  x + tileX/2 - 40, i*tileY/2 + tileY/2);

                    int[] xpts = {
                        tileX/2, tileX, tileX/2, 0
                    };
                    int[] ypts = {
                        0, tileY/2, tileY, tileY/2
                    };
                    Polygon mainTile = new Polygon(xpts, ypts, 4);
                    mainTile.translate(x, i*tileY/2);
                    graphics.drawPolygon(mainTile);
                }
            }
        }
    }
}

