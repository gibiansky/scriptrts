package com.scriptrts.core.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import javax.swing.JPanel;

import com.scriptrts.core.Main;
import com.scriptrts.game.GameObject;
import com.scriptrts.game.GameMap;
import com.scriptrts.game.MapGrid;
import com.scriptrts.game.TerrainType;

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
    private BufferedImage image;
    
    /**
     * Final minimap image drawn to screen
     */
    private BufferedImage minimapImage;
    
    /**
     * Cached map image used to generate minimap
     */
    private BufferedImage mapImage;

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
        graphics.drawImage(minimapImage, 0, 0, null);


        if(changedMinimap && Main.getGame().getCurrentMap() != null)
            redrawMinimap();

        /* Draw viewport */
        graphics.setColor(Color.white);
        double scale = ((double) viewport.getMapX()) / width;
        int x = (int) (viewport.getX() / scale);
        int y = (int) (viewport.getY() / scale);
        int w = (int) (viewport.getWidth() / scale);
        int h = (int) (viewport.getHeight() / scale);

        /* Center the viewport around the place where the user clicks */
        x -= w / 2;
        y -= h / 2;

        graphics.drawRect(x, y, w, h);
        graphics.drawRect(x-1, y-1, w+2, h+2);

    }

    /**
     * Calculate the minimap image
     */
    private void redrawMinimap(){
        /* After this redraw, no more updated needed until next change */
        changedMinimap = false;
        
        if(image == null)
        	image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        /* Draw a temporary square map */
        double size = (width / Math.sqrt(2));
        if(mapImage == null)
        	mapImage = new BufferedImage((int) size, (int) size, BufferedImage.TYPE_INT_ARGB);
        
        BufferedImage temporary = mapImage;
        Graphics2D graphics = (Graphics2D) temporary.getGraphics();

        int n = Main.getGame().getCurrentMap().getN();
        GameMap map = Main.getGame().getCurrentMap();
        TerrainType[][] terrain = map.getTileArray();
        double squareSize = size / n;

        /* Draw terrain */
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                Color c = terrain[i][j].getMinimapColor();

                GameObject u;
                for(int a = 0; a < MapGrid.SPACES_PER_TILE; a++)
                    for(int b = 0; b < MapGrid.SPACES_PER_TILE; b++)
                        if((u = Main.getGame().getGameGrid().getUnit(i * MapGrid.SPACES_PER_TILE + a, j * MapGrid.SPACES_PER_TILE + b)) != null && u.getUnit().getAllegiance().getID() != -1)
                            c = u.getUnit().getAllegiance().getColor();

                graphics.setColor(c);

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
        minimapImage = blur.filter(image, minimapImage);
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
