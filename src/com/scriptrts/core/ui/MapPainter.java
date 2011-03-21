package com.scriptrts.core.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import com.scriptrts.core.Main;
import com.scriptrts.game.Map;
import com.scriptrts.game.MapGrid;
import com.scriptrts.game.TerrainType;
import com.scriptrts.util.ResourceManager;

/**
 * Painter which displays map terrain tiles on the screen.
 */
public class MapPainter {
    /**
     * Whether or not to draw debug lines on the map. Initialized to false;
     */
    public static boolean DEBUG = false;

    /**
     * Whether or not to use fog of war. Initialized to false;
     */
    public static boolean USE_FOG_OF_WAR = false;
    /**
     * Whether to disable masking (for speed purposes). Initialized to false.
     */
    public static boolean NO_MASKING = false;

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
     * A black terrain image used for masking the edges of the map.
     */
    private BufferedImage blackImage;

    /**
     * The list of texture images, scaled to current tile size.
     */
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

    /**
     * An array of masks, scaled to current tile size.
     */
    private BufferedImage[][] scaledTerrainMasks;

    /**
     * An array of masks used to mask the edge of the map.
     */
    private BufferedImage[] blackMasks;

    /**
     * The black edge maps, scaled.
     */
    private BufferedImage[] scaledBlackMasks;

    /**
     * Flags that dictate the order that the masks are stored in the mask arrays 
     */
    private static final int MASK_TOP = 0, MASK_BOTTOM = 1, MASK_LEFT = 2, MASK_RIGHT = 3,
            MASK_TOP_LEFT = 4, MASK_TOP_RIGHT = 5, MASK_BOTTOM_LEFT = 6, MASK_BOTTOM_RIGHT = 7;

    /**
     * An array which we can loop over to iterate over all mask types
     */
    private static final int[] MASKS = 
    {
        MASK_TOP, MASK_BOTTOM, MASK_LEFT, MASK_RIGHT, MASK_TOP_LEFT, MASK_TOP_RIGHT, MASK_BOTTOM_LEFT, MASK_BOTTOM_RIGHT
    };

    /**
     * An array of black and white layer mask images that are applied to textures to create the necessary masks 
     */
    private static BufferedImage[] maskImages;

    /**
     * Integer array used for internal storage by paintMap() to avoid creating many new integer arrays every time.
     */
    private int[] mapBoundsPaintArray = new int[4];

    /**
     * Constructor which takes a map and an initial tile size, and initializes all necessary elements of the painter
     * @param map map to draw
     * @param tileX horizontal size of each tile
     * @param tileY vertical size of each tile (should be half of horizontal size)
     */
    public MapPainter(Map map, int tileX, int tileY){
        /* Store fields */
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

            blackImage = ResourceManager.loadImage("resource/map/" + "Black" + ".png", MAX_TILE_X, MAX_TILE_Y);
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
        blackMasks = new BufferedImage[MASKS.length];
        scaledBlackMasks = new BufferedImage[MASKS.length];
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


        /* Black masks */
        BufferedImage originalImg = blackImage;

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
            blackMasks[j] = maskImg;
            scaledBlackMasks[j] = maskImg;
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
                int newIndexX = i;

                /* We have to change how we get the y index depending on
                 * our x-index because every other row is shifted to the right */
                int newIndexY = j + 1;

                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_TOP_RIGHT);

                /* Bottom left side of the tile */
                newIndexX = i;
                newIndexY = j - 1;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_BOTTOM_LEFT);

                /* Mask sides going from the top left to bottom right */
                /* Top left side of the tile */
                newIndexX = i -1;
                newIndexY = j;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_TOP_LEFT);

                /* Bottom right side of the tile */
                newIndexX = i + 1;
                newIndexY = j;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_BOTTOM_RIGHT);

                /* Top  corner */
                newIndexX = i - 1;
                newIndexY = j + 1;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length){
                    if(terrain[i][j] < terrain[newIndexX][newIndexY]) {
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_TOP);
                    }
                }

                /* Bottom corner */
                newIndexX = i + 1;
                newIndexY = j - 1;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_BOTTOM);

                /* Left  corner */
                newIndexX = i - 1;
                newIndexY = j - 1;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j,  terrain[newIndexX][newIndexY], MASK_LEFT);

                /* Right corner */
                newIndexX = i + 1;
                newIndexY = j + 1;
                if(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length)
                    if(terrain[i][j] < terrain[newIndexX][newIndexY])
                        enableMasking(i, j, terrain[newIndexX][newIndexY], MASK_RIGHT);
            }
        }

        /* After real masks are applied, apply black masks on edges of map */
        int blackTerrain = -1000;
        for(int i = 0; i < terrain.length; i++){
            for(int j = 0; j < terrain[0].length; j ++){

                /* Top right side of the tile */
                int newIndexX = i;

                /* We have to change how we get the y index depending on
                 * our x-index because every other row is shifted to the right */
                int newIndexY = j + 1;

                if(!(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length))
                    enableMasking(i, j, blackTerrain, MASK_TOP_RIGHT);

                /* Bottom left side of the tile */
                newIndexX = i;
                newIndexY = j - 1;
                if(!(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length))
                    enableMasking(i, j, blackTerrain, MASK_BOTTOM_LEFT);

                /* Mask sides going from the top left to bottom right */
                /* Top left side of the tile */
                newIndexX = i -1;
                newIndexY = j;
                if(!(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length))
                    enableMasking(i, j, blackTerrain, MASK_TOP_LEFT);

                /* Bottom right side of the tile */
                newIndexX = i + 1;
                newIndexY = j;
                if(!(newIndexX >= 0 && newIndexX < terrain.length && newIndexY >= 0 && newIndexY < terrain[0].length))
                    enableMasking(i, j, blackTerrain, MASK_BOTTOM_RIGHT);


            }
        }

    }

    /**
     * Enable masking for the given tile with a certain type of mask and a certain masking terrain
     * @param i x coordinate of map tile
     * @param j y coordinate of map tile
     * @param maskingTerrain which terrain type to mask over with
     * @param maskID which mask to apply
     */
    private void enableMasking(int i, int j, int maskingTerrain, int maskID){
        masking[i][j][maskID] = maskingTerrain;
    }

    /**
     * Retrieve the mask image for this terrain and this type of mask
     * @param terrain the type of terrain to mask with
     * @param maskID which type of mask to apply
     */
    private BufferedImage getMask(int terrain, int maskID){
        int blackMask = -1000;
        if(terrain == blackMask)
            return blackMasks[maskID];
        else
            return scaledTerrainMasks[terrain][maskID];
    }

    /**
     * Get the graphical coordinates of a given tile
     * @param i x coordinate of tile
     * @param j y coordinates of tile
     * @return (x, y) graphical screen coordinates of tile
     */
    public Point getTileCoordinates(int i, int j){
        /* Coordinates of top corner of tile */
        int x = (i+j+1)*tileX/2;
        int y = tileY * Main.getGame().getCurrentMap().getN() / 2 + (i - j - 1) * tileY / 2;

        /* Coordinates of tile image */
        return new Point(x - tileX / 2, y);
    }


    /**
     * Get the width of the tiles
     * @return tile width
     */
    public int getTileWidth(){
        return tileX;
    }

    /**
     * Get the height of the tiles
     * @return tile height
     */
    public int getTileHeight(){
        return tileY;
    }

    /**
     * Get the map this painter draws
     * @return map drawn by this painter
     */
    public Map getMap(){
        return Main.getGame().getCurrentMap();
    }

    /**
     * Get the tile that is at a given point on the map
     * @param point screen coordinates of the desired tile
     * @param viewport viewport that is being used to view map
     * @return (i, j) map coordinates of tile that was clicked on
     */

    public Point getTileAtPoint(Point point, Viewport viewport){   	
        int mapHeight = tileY * Main.getGame().getCurrentMap().getN();

        /* Avoid modifying the input point, in case they will be used later by the caller */
        point = new Point(point);
        /* Point in map coordinates */
        point.translate(viewport.getX(), viewport.getY());

        /* Solve the linear transformation */
        double term1 = (double) (point.x) / (double) (tileX / 2);
        double term2 = (double) (point.y - mapHeight / 2) / (double) (tileY / 2);

        int mapX = (int) ((term1 + term2) / 2);
        int mapY = (int) ((term1 - term2) / 2);

        /* If tile is not on map, return null */
        if(mapX < 0 || mapX > Main.getGame().getCurrentMap().getN() || mapY < 0 || mapY > Main.getGame().getCurrentMap().getN())
            return null;

        Point mapTile = new Point(mapX, mapY);

        return mapTile;
    }

    /**
     * Signifies that the input map to this map painter was changed, and that the map painter
     * should change its internal state to reflect the change in the map.
     */
    public synchronized void update(){
        TerrainType[][] terrainTypes = Main.getGame().getCurrentMap().getTileArray();
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
        getViewportTileBounds(mapBoundsPaintArray, viewport);
        int west    = mapBoundsPaintArray[0];
        int east   = mapBoundsPaintArray[1];
        int south    = mapBoundsPaintArray[2];
        int north   = mapBoundsPaintArray[3];

        /* Only draw what we need to */
        for(int i = west; i < east; i++) {
            for(int j = south; j < north; j++) {
                /*
                 * Calculate the locations of the back corners of the tiles.
                 */
                int x = (i+j+1)*tileX/2;
                int y = tileY * Main.getGame().getCurrentMap().getN() / 2 + (i - j - 1) * tileY / 2;

                /* Draw the tile */
                Image image = scaledImages[terrain[i][j]];
                
                byte[][] vis = Main.getGame().getPlayer().getVisibilityGrid();

                /* Figure out if any part of the tile is visible,
                 * and if it is, make the whole map tile visible */
                boolean partVis = false;
                for(int k = 0; k < MapGrid.SPACES_PER_TILE; k++)
                    for(int l = 0; l < MapGrid.SPACES_PER_TILE; l++)
                        if(vis[i * MapGrid.SPACES_PER_TILE + k][j * MapGrid.SPACES_PER_TILE + l] == 2)
                            partVis = true;

                /* If the tile is visible to a unit, paint it normally */
                if(partVis || !USE_FOG_OF_WAR)
                    graphics.drawImage(image, x - tileX / 2, y, tileX, tileY, null);
                /* If the tile is not visible to units, paint it black */
                else
                    graphics.drawImage(blackImage, x - tileX / 2, y, tileX, tileY, null);

                /* Don't mask above a certain zoom level */
                if(!NO_MASKING)
                    if(tileX > 16 && tileY > 8){
                        /* Draw the masks on top of the tile */
                        for(int texType = 0; texType < images.length; texType++){
                            for(int maskID : MASKS){
                                int maskingTerrain = masking[i][j][maskID];

                                /* If the terrain is less than zero, that means we don't need any masking at all */
                                if(maskingTerrain == texType){
                                    BufferedImage mask = getMask(maskingTerrain, maskID);
                                    graphics.drawImage(mask, x - tileX / 2, y, tileX, tileY, null);
                                }
                            }
                        }

                        /* Draw black masks on top of all other masks */
                        int blackMask = -1000;
                        for(int maskID : MASKS){
                            int maskingTerrain = masking[i][j][maskID];

                            /* If the terrain is less than zero, that means we don't need any masking at all */
                            if(maskingTerrain == blackMask){
                                BufferedImage mask = getMask(maskingTerrain, maskID);
                                graphics.drawImage(mask, x - tileX / 2, y, tileX, tileY, null);
                            }
                        }
                    }

                /* Draw debug lines and labels */
                if(MapPainter.DEBUG){
                    graphics.setColor(Color.RED);
                    graphics.drawString("(" + i + ", " + j + ": " + terrain[i][j] + ")",  x - 50, y + tileY/2);

                    int[] xpts = {
                        tileX/2, tileX, tileX/2, 0
                    };
                    int[] ypts = {
                        0, tileY/2, tileY, tileY/2
                    };
                    Polygon mainTile = new Polygon(xpts, ypts, 4);
                    mainTile.translate(x - tileX/2, y);
                    graphics.drawPolygon(mainTile);
                }
            }
        }
    }

    /**
     * Get the bounds on the viewport in terms of tiles
     * @param bounds integer array in which the bounds are stored
     * @param viewport viewport used to display map
     */
    public void getViewportTileBounds(int[] bounds, Viewport viewport){
        if(bounds.length != 4) return;	

        double x = viewport.getX();
        double y = viewport.getY();
        double width = viewport.getWidth();
        double height = viewport.getHeight();
        int n = Main.getGame().getCurrentMap().getN();

        /* Find minimum i using (x, y) */
        double j = (x / (tileX/2) - 2 - (y - tileY * n / 2) / (tileY / 2))/2;
        double i = (x/(tileX/2)) - j - 1;
        int iMin = (int)(i) - 1;

        /* Find minimum j using (x, y+height) */
        j = (x / (tileX/2) - 2 - ((y + height) - tileY * n / 2) / (tileY / 2))/2;
        i = (x/(tileX/2)) - j - 1;
        int jMin = (int)(j) - 1;

        /* Find maximum i using (x+width, y+height) */
        j = ((x+width) / (tileX/2) - 2 - ((y+height) - tileY * n / 2) / (tileY / 2))/2;
        i = ((x+width)/(tileX/2)) - j - 1;
        int iMax = (int)(i) + 2;

        /* Find maximum j using (x+width, y) */
        j = ((x+width) / (tileX/2) - 2 - (y - tileY * n / 2) / (tileY / 2))/2;
        i = ((x+width)/(tileX/2)) - j - 1;
        int jMax = (int)(j) + 2;

        if(iMin < 0) iMin = 0;
        if(jMin < 0) jMin = 0;
        if(iMax < 0) iMax = 0;
        if(jMax < 0) jMax = 0;
        if(iMin > n) iMin = n;
        if(jMin > n) jMin = n;
        if(iMax > n) iMax = n;
        if(jMax > n) jMax = n;

        bounds[0] = iMin;
        bounds[1] = iMax;
        bounds[2] = jMin;
        bounds[3] = jMax;
    }
}

