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

}
