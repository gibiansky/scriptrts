package com.scriptrts.game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

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
     * Create a new animated sprite with the specified number of frames (to add on frame by frame later).
     */
    public AnimatedSprite(int n, double scale){
    	super(null, scale, 0, 0);
    	images = new BufferedImage[n];
    	times = new int[n];
    	backXs = new int[n];
    	backYs = new int[n];
    }

    /**
     * Draw the animated sprite. This method uses the unit parameter to determine which part of the animation to draw.
     * @param graphics graphics handle used to draw to the screen
     * @param unit the unit which this sprite is being drawn for
     * @param tileBackX the back corner x coordinate of the unit tile on which this is being drawn
     * @param tileBackY the back corner y coordinate of the unit tile on which this is being drawn
     */
    public void draw(Graphics2D graphics, GameObject unit, int tileBackX, int tileBackY){
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
    
    /**
     * Add a frame to the current animated sprite
     * @param frame frame number to add
     * @param image image to add
     * @param duration how long to show the image (in frames)
     * @param backX the back x coordinate of the image
     * @param backY the back y coordinate of the image
     */
    public void addFrame(int frame, BufferedImage image, int duration, int backX, int backY){	
    	images[frame - 1] = image;
    	backXs[frame - 1] = backX;
    	backYs[frame - 1] = backY;
    	
    	for(int i = frame - 1; i < times.length; i++){
    		times[i] += duration;
    	}
    	
    	/* If this image is the first frame of the animation, set the sprite's default image and back coordinates */
    	if(frame == 1){
    		super.setImage(image);
    		super.setBackCoordinates(backX, backY);
    	}
    }
    
    public void replaceFrame(int frame, BufferedImage image, int duration, int backX, int backY){
    	if(getFrameImage(frame) == null)
    		return;
    	int oldDuration = times[frame - 1];
    	for(int i = frame - 1; i < times.length; i++){
    		times[i] -= oldDuration;
    	}
    	addFrame(frame, image, duration, backX, backY);    	
    }
    
    /**
     * Get the number of frames in this animation
     * @return the number of frames in this animation
     */
    public int getNumberFrames(){
    	return images.length;
    }
    
    /**
     * Get the image at the given frame
     * @param frame frame to check
     * @return image at the given frame
     */
    public BufferedImage getFrameImage(int frame){
    	return images[frame - 1];
    }
    
    /**
     * Get the duration the given frame is shown for
     * @param frame frame to check
     * @return duration of the given frame
     */
    public int getFrameDuration(int frame){
    	if(frame == 1)
    		return times[0];
    	else
    		return times[frame - 1] - times[frame - 2];
    }
    
    /**
     * Get the back x coordinate of this frame
     * @param frame frame to check
     * @return back x coordinate of the given frame
     */
    public int getFrameBackX(int frame){
    	return backXs[frame - 1];
    }
    
    /**
     * Get the back y coordinate of this frame
     * @param frame frame to check
     * @return back y coordinate of the given frame
     */
    public int getFrameBackY(int frame){
    	return backYs[frame - 1];
    }
}
