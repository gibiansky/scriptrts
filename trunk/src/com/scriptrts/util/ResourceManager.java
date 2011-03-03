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

/**
 * Utility resource manager to load images, audio, and data files
 */
public class ResourceManager {

    /**
     * Cache of loaded images (hashed with their filenames as keys)
     */
    private static HashMap<String, BufferedImage> imageCache = new HashMap<String, BufferedImage>();

    /**
     * Load a scaled image texture from the provided filename
     * @param filename image filename
     * @param width horizontal size
     * @param height vertical size
     * @return scaled version of the image
     */
    public static BufferedImage loadImage(String filename, int width, int height) throws IOException {
        String id = filename + " (" + width + ", " + height + ")";
        if(imageCache.containsKey(id))
            return imageCache.get(id);

        BufferedImage unscaledTexture = ResourceManager.loadImage(filename);

        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) scaledImage.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(unscaledTexture, 0, 0, width, height, null);
        g.dispose();

        imageCache.put(id, scaledImage);
        return scaledImage;
    }

    /**
     * Reads an image from a file
     * @param filename image filename
     * @return BufferedImage read from the file
     */
    public static BufferedImage loadImage(String filename) throws IOException {
        if(imageCache.containsKey(filename))
            return imageCache.get(filename);

        BufferedImage image = ImageIO.read(new File(filename));

        imageCache.put(filename + " (" + image.getWidth() + ", " + image.getHeight() + ")", image);
        imageCache.put(filename, image);
        return image;
    }

    /**
     * Reads an association file
     * @param filename
     * @return HashMap which associates strings with strings
     */
    public static HashMap<String, String> readAssociationFile(String filename){
        HashMap<String, String> associations = new HashMap<String, String>();
        try {
            /* The format of the file we're reading is any number of lines, where the first word is 
             * the name (in the enum) of the tile terrain. The first word is followed by a ", ", followed by
             * the filename of the image texture (sans ".png"). For instance,
             *      Dirt, Martian Rock
             *      Grass, Prarie Grass
             *      Water, Blue Aqua
             *      ... and so on ...
             */
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String str;
            while ((str = reader.readLine()) != null) {
                String[] line = str.split(",");
                String key = line[0].trim();
                String value = line[1].trim();
                associations.put(key, value);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return associations;
    }
}
