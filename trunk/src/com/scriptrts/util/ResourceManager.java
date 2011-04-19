package com.scriptrts.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.scriptrts.game.AnimatedSprite;
import com.scriptrts.game.Player;
import com.scriptrts.game.Sprite;

/**
 * Utility resource manager to load images, audio, and data files
 */
public class ResourceManager {

    /**
     * Cache of loaded images (hashed with their filenames as keys)
     */
    private static HashMap<String, BufferedImage> imageCache = new HashMap<String, BufferedImage>();

    /**
     * Scale an image to a new dimension
     * @param width horizontal size
     * @param height vertical size
     * @return scaled version of the image
     */
    public static BufferedImage scaleImage(BufferedImage unscaled, int width, int height){
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) scaledImage.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(unscaled, 0, 0, width, height, null);
        g.dispose();

        return scaledImage;
    }

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

        try {
            BufferedImage image = ImageIO.read(new File(filename));

            imageCache.put(filename + " (" + image.getWidth() + ", " + image.getHeight() + ")", image);
            imageCache.put(filename, image);
            return image;
        } catch (IOException e){
            System.out.println("Failed to load: " + filename);
            throw e;
        }
    }

    /**
     * Reads an image from a file, then reads a color mask to apply from another file, and returns the combined image.
     * @param filename image filename
     * @param band color band image filename
     * @param color color to band the image with
     * @return BufferedImage read from file with applied color band
     */
    public static BufferedImage loadBandedImage(String filename, String band, Color color) throws IOException {
    	BufferedImage original = loadImage(filename);
    	
    	String id = filename + " - banded " + band + " (" + original.getWidth() + ", " + original.getHeight() + ")";
    	if(imageCache.containsKey(id))
            return imageCache.get(id);
    	
        BufferedImage mask = loadImage(band);

        /* Create the band image */
        BufferedImage bandImg = new BufferedImage(mask.getWidth(), mask.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for(int pixel_i = 0; pixel_i < mask.getWidth(); pixel_i++){
            for(int pixel_j = 0; pixel_j < mask.getHeight(); pixel_j++){
                /* Get colors of mask image and mask itself */
                int maskColor = mask.getRGB(pixel_i, pixel_j);

                /* Extract the pieces of the color */
                int intensity = ((maskColor & 0x00FF0000) >> 16); 
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();

                /* Black means that the alpha is high, white means the alpha is low */
                int newColor = (intensity << 24) | (red << 16) | (green << 8) | blue;

                bandImg.setRGB(pixel_i, pixel_j, newColor);
            }
        }

        BufferedImage bandedImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
        bandedImage.getGraphics().drawImage(original, 0, 0, null);
        bandedImage.getGraphics().drawImage(bandImg, 0, 0, null);
        bandedImage.getGraphics().dispose();

        imageCache.put(id, bandedImage);
        return bandedImage;
    }
    /**
     * Load a spriteset from a sprite definition file.
     * @param filename filename of sprite definition in the sprite resource directory
     * @param player player to whom the sprite will belong, needed to apply banding
     * @return array of sprites
     */
    public static Sprite[] loadSpriteSet(String filename, Player player) throws IOException {
        final String defineDir = "resource/spritesets/";

        Scanner reader = new Scanner(new File(defineDir + filename));

        int numSprites = reader.nextInt();
        Sprite[] sprites = new Sprite[numSprites];

        float scale = reader.nextFloat();
        for(int i = 0; i < numSprites; i++){
            sprites[i] = readSprite(reader, scale, player);
        }

        reader.close();
        return sprites;
    }

    /**
     * Read a sprite defined in the given definition file stream.
     * @param reader scanner used to read from the definition file
     * @param scale scale to apply to sprite
     * @param player player to use for banding
     * @return sprite as defined by definition file
     */
    private static Sprite readSprite(Scanner reader, float scale, Player player) throws IOException {
        final String imageDir = "resource/";

        String type = readString(reader).trim();
        if(type.equals("NULL"))
            return null;

        if(type.equals("STATIC")){
            int x = reader.nextInt();
            int y = reader.nextInt();
            String filename = readString(reader);
            return new Sprite(loadImage(imageDir + filename), scale, x, y);
        }

        if(type.equals("STATIC BAND")){
            int x = reader.nextInt();
            int y = reader.nextInt();
            String filename = readString(reader);
            String bandFilename = readString(reader);
            return new Sprite(loadBandedImage(imageDir + filename, imageDir + bandFilename, player.getColor()), scale, x, y);
        }

        if(type.equals("ANIMATED")){
            int stages = reader.nextInt();
            int[] durations = new int[stages];
            int[] xs = new int[stages];
            int[] ys = new int[stages];
            BufferedImage[] imgs = new BufferedImage[stages];

            for(int i = 0; i < stages; i++){
                durations[i] = reader.nextInt();
                xs[i] = reader.nextInt();
                ys[i] = reader.nextInt();
                imgs[i] = loadImage(imageDir + readString(reader));
            }

            return new AnimatedSprite(imgs, durations, scale, xs, ys);
        }

        if(type.equals("ANIMATED BAND")){
            int stages = reader.nextInt();
            int[] durations = new int[stages];
            int[] xs = new int[stages];
            int[] ys = new int[stages];
            BufferedImage[] imgs = new BufferedImage[stages];

            for(int i = 0; i < stages; i++){
                durations[i] = reader.nextInt();
                xs[i] = reader.nextInt();
                ys[i] = reader.nextInt();
                imgs[i] = loadBandedImage(imageDir + readString(reader), imageDir + readString(reader), player.getColor());
            }

            return new AnimatedSprite(imgs, durations, scale, xs, ys);
        }

        throw new RuntimeException("Tried to load unknown sprite type: '" + type + "'");
    }

    /**
     * Read a non-blank line from this scanner.
     * @param scanner scanner to read from
     * @return non-blank non-whitespace line
     */
    private static String readString(Scanner scanner) throws IOException {
        String nextLine;
        while((nextLine = scanner.nextLine()).trim().equals(""));
        return nextLine;
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
