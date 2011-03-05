package com.scriptrts.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.awt.image.ConvolveOp;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

import com.scriptrts.game.UnitGrid;
import com.scriptrts.game.SimpleUnit;

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
     * Viewport currently showing map
     */
    private Viewport viewport;

    /**
     * Whether the minimap needs to be updated with new information about the map
     */
    private static boolean changedMinimap = true;

    /**
     * Create a new minimap
     * @param viewport to show on minimap
     */
    public Minimap(Viewport viewport){
        super(true);

        this.viewport = viewport;
        addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent mouse){
                moveViewportOnClick(mouse.getX(), mouse.getY());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseDragged(MouseEvent mouse){
                moveViewportOnClick(mouse.getX(), mouse.getY());
            }
        });

    }

    /**
     * Tell the minimaps to redraw
     */
    public static void updateMinimap(){
        changedMinimap = true;
    }

    /**
     * Draw the minimap on the screen
     * @param g graphics handle for the screen
     */
    public void paintComponent(Graphics g){
        Graphics2D graphics = (Graphics2D) g;
        graphics.drawImage(image, 0, 0, null);


        if(changedMinimap && Main.getGame().getCurrentMap() != null)
            redrawMinimap();

        /* Draw viewport */
        graphics.setColor(Color.black);
        double scale = ((double) viewport.getMapX()) / width;
        int x = (int) (viewport.getX() / scale);
        int y = (int) (viewport.getY() / scale);
        int w = (int) (viewport.getWidth() / scale);
        int h = (int) (viewport.getHeight() / scale);
        graphics.drawRect(x, y, w, h);
        graphics.drawRect(x-1, y-1, w+2, h+2);

    }

    /**
     * Calculate the minimap image
     */
    private void redrawMinimap(){
        /* After this redraw, no more updated needed until next change */
        changedMinimap = false;

        /* Draw a temporary square map */
        double size = (width / Math.sqrt(2));
        BufferedImage temporary = new BufferedImage((int) size, (int) size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) temporary.getGraphics();

        int n = Main.getGame().getCurrentMap().getN();
        Map map = Main.getGame().getCurrentMap();
        TerrainType[][] terrain = map.getTileArray();
        double squareSize = size / n;

        /* Draw terrain */
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                Color c = terrain[i][j].getMinimapColor();

                SimpleUnit u;
                for(int a = 0; a < UnitGrid.SPACES_PER_TILE; a++)
                    for(int b = 0; b < UnitGrid.SPACES_PER_TILE; b++)
                        if((u = Main.getGame().getUnitGrid().getUnit(i * UnitGrid.SPACES_PER_TILE + a, j * UnitGrid.SPACES_PER_TILE + b)) != null)
                            c = u.getAllegiance().getColor();

                graphics.setColor(c);

                int roundedSize = (int) squareSize;
                graphics.fillRect((int) (i * squareSize), (int) (j * squareSize), (int) (squareSize+1), (int)(squareSize+1));
            }
        }


        /* Transform the map into the minimap */
        AffineTransform transform = new AffineTransform();
        transform.scale(-1, .5);
        transform.translate(temporary.getWidth() / 2 / Math.sqrt(2), temporary.getHeight() / 2 / Math.sqrt(2));
        transform.rotate(5*Math.PI/2 + Math.PI / 4);
        transform.translate(-temporary.getWidth() / 2, -temporary.getHeight() / 2);

        /* Draw on the real minimap */
        graphics = (Graphics2D) image.getGraphics();

        /* Draw the background */
        int[] xs = new int[]{
            0, width/2, width, width/2
        };
        int[] ys = new int[]{
            height/2, 0, height/2, height
        };
        Polygon poly = new Polygon(xs, ys, 4);
        graphics.fillPolygon(poly);

        /* Paint the minimap */
        AffineTransform previous = graphics.getTransform();
        graphics.setTransform(transform);
        int shiftX = temporary.getWidth();
        int shiftY = temporary.getHeight() / 2;
        graphics.drawImage(temporary, shiftX, shiftY, temporary.getWidth() + 2, temporary.getHeight() + 2, null);
        graphics.setTransform(previous);

        /* Blur the map */
        float[] data = {
           0.125f,0.125f,0.125f,0.125f, .25f, 0.125f,0.125f,0.125f,0.125f
        };
        Kernel kernel = new Kernel(3, 3, data);
        ConvolveOp blur = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        image = blur.filter(image, null);
    }

    /**
     * Move the viewport to the clicked tile
     * @param x x coordinate of click
     * @param y y coordinate of click
     */
    public void moveViewportOnClick(int x, int y){
        double scale = ((double) viewport.getMapX()) / width;
        int xView = (int) (x * scale);
        int yView = (int) (y * scale);
        viewport.setLocation(xView, yView);
    }

    /**
     * Get the desired size of the minimap
     */
    public Dimension getPreferredSize(){
        return new Dimension(width, height);
    }
}
