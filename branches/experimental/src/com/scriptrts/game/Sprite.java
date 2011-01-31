package com.scriptrts.game;

import java.awt.Rectangle;
import com.scriptrts.core.UnitPainter;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Sprite {
    private BufferedImage image;
    private double scale;
    private int spriteBackX;
    private int spriteBackY;

    public Sprite(BufferedImage img, double defaultScale, int backX, int backY){
        super();
        this.image = img;
        this.scale = defaultScale;
        this.spriteBackX = backX;
        this.spriteBackY = backY;
    }

    public void draw(Graphics2D graphics, int tileBackX, int tileBackY){
        int bX = (int) (spriteBackX * scale);
        int bY = (int) (spriteBackY * scale);

        graphics.drawImage(image, tileBackX - bX, tileBackY - bY, getWidth(), getHeight(), null);

        if(UnitPainter.DEBUG){
            graphics.setColor(java.awt.Color.green);
            graphics.fillRect(tileBackX, tileBackY, 5, 5);
        }
    }

    public void scale(double scaleFactor){
        scale *= scaleFactor;
    }

    public int getWidth(){
        return (int) (image.getWidth() * scale);
    }

    public int getHeight(){
        return (int) (image.getHeight() * scale);
    }

    public Rectangle getBounds(int tileBackX, int tileBackY){
        int xLoc = (int) (tileBackX - spriteBackX * scale);
        int yLoc = (int) (tileBackY - spriteBackY * scale);
        return new Rectangle(xLoc, yLoc, getWidth(), getHeight());
    }
}
