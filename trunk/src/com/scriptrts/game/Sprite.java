package com.scriptrts.game;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.scriptrts.core.ui.UnitPainter;

/**
 * Sprite, used by units to draw themselves on the game map
 */
public class Sprite {
    /**
     * Image used to draw the sprite.
     */
    private BufferedImage image;

    /**
     * Scale to apply to the sprite image.
     */
    private double scale;

    /**
     * Where the back corner of the sprite is
     */
    private int spriteBackX, spriteBackY;

    /**
     * Create a new sprite.
     * @param img image used to draw the sprite
     * @param defaultScale how much to scale the sprite by default
     * @param backX the back location on the sprite (x coordinate)
     * @param backY the back location on the sprite (y coordinate)
     */
    public Sprite(BufferedImage img, double defaultScale, int backX, int backY){
        super();
        this.image = img;
        this.scale = defaultScale;
        this.spriteBackX = backX;
        this.spriteBackY = backY;
    }

    /**
     * Draw the sprite image on the screen.
     * @param graphics graphics handle used to draw to the screen
     * @param unit the unit which this sprite is being drawn for
     * @param tileBackX the back corner x coordinate of the unit tile on which this is being drawn
     * @param tileBackY the back corner y coordinate of the unit tile on which this is being drawn
     */
    public void draw(Graphics2D graphics, GameObject unit, int tileBackX, int tileBackY){
        int bX = (int) (spriteBackX * scale);
        int bY = (int) (spriteBackY * scale);
        //System.out.println(tileBackX + " " + tileBackY + " " + bX + " " + bY);

        graphics.drawImage(image, tileBackX - bX, tileBackY - bY, getWidth(), getHeight(), null);

        if(UnitPainter.DEBUG){
            graphics.setColor(java.awt.Color.green);
            graphics.fillRect(tileBackX, tileBackY, 5, 5);
        }
    }


    /**
     * Draw the sprite image centered at the given point.
     * @param graphics graphics handle used to draw to the screen
     * @param x x coordinate on the screen to draw at
     * @param y y coordinate on the screen to draw at
     */
    public void drawCentered(Graphics2D graphics, int x, int y){
        graphics.drawImage(image, x - getWidth()/2, y - getHeight()/2, getWidth(), getHeight(), null);
    }

    /**
     * Scale this sprite relative to its normal scale
     * @param scaleFactor the factor by which to scale it
     */
    public void scale(double scaleFactor){
        scale *= scaleFactor;
    }

    /**
     * Set the image for this sprite to display itself as
     * @param image image used to draw the sprite
     */
    public void setImage(BufferedImage image){
        this.image = image;
    }

    /**
     * Set the back coordinates of this image
     * @param x back x
     * @param y back y
     */
    public void setBackCoordinates(int x, int y){
        spriteBackX = x;
        spriteBackY = y;
    }

    /**
     * Compute the width of this sprite
     * @return width of the sprite image when drawn
     */
    public int getWidth(){
        return (int) (image.getWidth() * scale);
    }

    /**
     * Compute the height of this sprite
     * @return height of the sprite image when drawn
     */
    public int getHeight(){
        return (int) (image.getHeight() * scale);
    }

    /**
     * Get the bounds of the drawn sprite image. 
     * @param tileBackX the back corner x coordinate of the unit tile on which this is being drawn
     * @param tileBackY the back corner y coordinate of the unit tile on which this is being drawn
     * @return the boundaries of the sprite image
     */
    public Rectangle getBounds(int tileBackX, int tileBackY){
        int xLoc = (int) (tileBackX - spriteBackX * scale);
        int yLoc = (int) (tileBackY - spriteBackY * scale);
        return new Rectangle(xLoc, yLoc, getWidth(), getHeight());
    }
}
