package com.scriptrts.game;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

/**
 * An animated sprite which switches between different images depending on the frame count
 */
public class AnimatedSprite extends Sprite {
    private BufferedImage[] images;
    private int[] times;
    private int[] backXs;
    private int[] backYs;

    /**
     * Create a new animated sprite.
     * @param images images to use for different parts in the animation
     * @param durations how long to show each image (in frames)
     * @param scale what scale to apply to all the images
     * @param backXs the back x coordinates of the images
     * @param backYs the back y coordinates of the images
     */
    public AnimatedSprite(BufferedImage[] images, int[] durations, double scale, int[] backXs, int[] backYs){
        super(images[0], scale, backXs[0], backYs[0]);

        this.images = images;
        this.backXs = backXs;
        this.backYs = backYs;

        /* Compute at what times to switch the images */
        times = new int[durations.length];
        int counter = 0;
        for(int i = 0; i < durations.length; i++){
            counter += durations[i];
            times[i] = counter;
        }
    }

    /**
     * Draw the animated sprite. This method uses the unit parameter to determine which part of the animation to draw.
     * @param graphics graphics handle used to draw to the screen
     * @param unit the unit which this sprite is being drawn for
     * @param tileBackX the back corner x coordinate of the unit tile on which this is being drawn
     * @param tileBackY the back corner y coordinate of the unit tile on which this is being drawn
     */
    public void draw(Graphics2D graphics, SimpleUnit unit, int tileBackX, int tileBackY){
        int frame = unit.getSpriteAnimation();
        /* If the animation has progressed beyond its bounds, reset it */
        if(frame > times[times.length - 1])
            unit.resetSpriteAnimation(frame % times[times.length - 1]);

        /* Set the image we want to draw */
        for(int i = 0; i < times.length; i++){
            int time = times[i];
            if(time <= frame){
                setImage(images[i]);
                setBackCoordinates(backXs[i], backYs[i]);
            }
        }

        /* Draw the sprite */
        super.draw(graphics, unit, tileBackX, tileBackY);
    }
}
