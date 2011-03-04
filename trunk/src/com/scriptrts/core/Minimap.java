package com.scriptrts.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.event.KeyListener;
import javax.swing.JPanel;


/**
 * Minimap panel which displays a miniature colored version of the map on the overlay panel
 */
public class Minimap extends JPanel {
    /**
     * Width of the minimap
     */
    private int width = 400;

    /**
     * Height of the minimap
     */
    private int height = 200;

    /**
     * Cached minimap image
     */
    private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    /**
     * Create a new minimap
     */
    public Minimap(){
        super(true);
    }

    /**
     * Draw the minimap on the screen
     * @param g graphics handle for the screen
     */
    public void paintComponent(Graphics g){
        Graphics2D graphics = (Graphics2D) g;
        redrawMinimap();
        graphics.drawImage(image, 0, 0, null);
    }

    /**
     * Calculate the minimap image
     */
    private void redrawMinimap(){

        /* Draw a temporary square map */
        double size = (width / Math.sqrt(2));
        BufferedImage temporary = new BufferedImage((int) size, (int) size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) temporary.getGraphics();

        int n = Main.getGame().getCurrentMap().getN();
        Map map = Main.getGame().getCurrentMap();
        TerrainType[][] terrain = map.getTileArray();
        double squareSize = size / n;

        graphics.setColor(Color.red);
        graphics.fillRect(0, 0, (int) size, (int) size);

        /* Draw terrain */
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                Color c = terrain[i][j].getMinimapColor();
                graphics.setColor(c);
                int roundedSize = (int) squareSize;
                graphics.fillRect((int) (i * squareSize), (int) (j * squareSize), (int) (squareSize+1), (int)(squareSize+1));
            }
        }

        /* Draw on the real minimap */
        graphics = (Graphics2D) image.getGraphics();

        /* Draw the background */
        graphics.setColor(Color.white);
        int[] xs = new int[]{0, width/2, width, width/2};
        int[] ys = new int[]{height/2, 0, height/2, height};
        Polygon poly = new Polygon(xs, ys, 4);
        graphics.fillPolygon(poly);
        
        /* Transform the map into the minimap */
        AffineTransform transform = new AffineTransform();
        transform.scale(1, .5);
        transform.translate(temporary.getWidth() / 2 / Math.sqrt(2), temporary.getHeight() / 2 / Math.sqrt(2));
        transform.rotate(Math.PI / 4);
        transform.translate(-temporary.getWidth() / 2, -temporary.getHeight() / 2);

        /* Draw minimap */
        graphics.setTransform(transform);
        graphics.drawImage(temporary, temporary.getWidth() / 2, -1, temporary.getWidth() + 2, temporary.getHeight() + 2, null);

        /* Reset transform */
        graphics.setTransform(new AffineTransform());
    }

    /**
     * Get the desired size of the minimap
     */
    public Dimension getPreferredSize(){
        return new Dimension(width, height);
    }
}
