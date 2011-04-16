/**
 * INSTRUCTIONS:
 * Run SpriteLoader.java
 * Load an image
 * Click on the image to set the back tile coordinates (i.e. where the unit will be centered when placed)
 * Assign a hotkey to place the unit
 * Hold backslash "\" while placing a unit to go into "sprite helping mode"
 * Continue holding "\" to add any unit tiles you click on to the unit's shape
 * Press enter to print out the unit's shape
 * Press escape to load another image
 */

package com.scriptrts.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.scriptrts.core.ClickAction;
import com.scriptrts.core.Main;
import com.scriptrts.core.PlaceAction;
import com.scriptrts.core.ui.Action;
import com.scriptrts.core.ui.HotkeyManager;
import com.scriptrts.core.ui.InputManager;
import com.scriptrts.core.ui.UnitPainter;
import com.scriptrts.core.ui.Viewport;
import com.scriptrts.game.Direction;
import com.scriptrts.game.GameObject;
import com.scriptrts.game.MapGrid;
import com.scriptrts.game.Sprite;
import com.scriptrts.game.UnitClass;
import com.scriptrts.game.UnitShape;

public class SpriteLoader extends JPanel{

	private static JFrame window;
	private static InputManager manager;
	private static BufferedImage sprite;
	private static int mouseX, mouseY;
	private static String fileName;
	private static int width, height;
	private static boolean visible;
	private static boolean imageSelected;
	private static JLabel check;
	private static char keyTyped;
	
	public SpriteLoader(){
		super(new BorderLayout());
		/* Load the image from the file directory */
		JFileChooser browser = new JFileChooser(new File("resource/"));
		this.add(browser);
		
		/* Whether the user pressed "Ok" or "Cancel" */
		int returnValue = browser.showOpenDialog(this);
		File file = browser.getSelectedFile();
		
		/* If "Ok", load the image and set some variables */
		if(returnValue == JFileChooser.APPROVE_OPTION){
			try {
				sprite = ResourceManager.loadImage(file.toString());
				fileName = file.toString();
				width = sprite.getWidth();
				height = sprite.getHeight();
				visible = true;
			} catch (IOException e) {
				System.out.println("File not found.");
				visible = false;
				this.setVisible(visible);
				Main.getMain().getTopLevelAncestor().setVisible(true);
			}
		}
		/* Otherwise quit out of the selection screen */
		else {
			visible = false;
			this.setVisible(visible);
			Main.getMain().getTopLevelAncestor().setVisible(true);
		}
	}
	
	public static void main(final String[] args){
		/* Start game */		
		Thread thread = new Thread(){
			public void run(){
				Main.main(args);
				Main.getMain().getTopLevelAncestor().setVisible(false);
			}
		};
		thread.start();
		
		/* Wait a bit to make sure everything loads */
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/* Instantiate stuff */
		manager = Main.getGame().getInputManager();
		SpriteHelper spriteHelper = new SpriteHelper();
		UnitPainter unitPainter = Main.getGame().getUnitPainter();
		MapGrid grid = Main.getGame().getGameGrid();
		Viewport viewport = Main.getGame().getViewport();
		ClickAction clickAction = null;
		
		manager.registerKeyCode(KeyEvent.VK_BACK_SLASH);
		manager.registerKeyCode(KeyEvent.VK_ENTER);
		manager.registerKeyCode(KeyEvent.VK_ESCAPE);
		
		init();
		
		/* Wait for image to be selected */
		while(!imageSelected);
		
		while(true){			
			
			if(manager.getKeyCodeFlag(KeyEvent.VK_BACK_SLASH)){
				manager.clearKeyCodeFlag(KeyEvent.VK_BACK_SLASH);

				/* If placing a unit, load the unit's data into the sprite helper */
				if(Main.getGame().getClickAction() != null && !Main.getGame().getClickAction().equals(clickAction)){
					clickAction = Main.getGame().getClickAction();
					
					clickAction.getUnit().setUnitShape(UnitShape.SHAPE_1x1);
					spriteHelper.setUnit(clickAction.getUnit());
				}
				
				/* Add tiles to the unit's shape when clicked on */
				if(manager.getMouseDown() || manager.getMouseClicked()){
					Point point = manager.getMouseLocation();
					Point unitTile = unitPainter.unitTileAtPoint(point, viewport);

					if(clickAction != null){
						spriteHelper.setTileX(unitTile.x);
						spriteHelper.setTileY(unitTile.y);
						
						clickAction = null;
					}
					
					/* Only add if the shape doesn't already contain this tile */
					if(spriteHelper != null && !spriteHelper.contains(unitTile.x, unitTile.y)){
						spriteHelper.addSquare(unitTile.x, unitTile.y, spriteHelper.getUnit().getFacingDirection());
					}
					grid.setUnit(spriteHelper.getUnit(), unitTile.x, unitTile.y);
				}
			}

			/* Print out the unit's shape in the current direction it's facing */
			if(manager.getKeyCodeFlag(KeyEvent.VK_ENTER)){
				manager.clearKeyCodeFlag(KeyEvent.VK_ENTER);
				if(spriteHelper.getUnit() != null){
					System.out.println(spriteHelper.getUnitShape());
				}
				spriteHelper = new SpriteHelper();
			}
			
			/* Load another sprite */
			if(manager.getKeyCodeFlag(KeyEvent.VK_ESCAPE)){
				init();
			}
		}
	}
	
	public static void init(){
		/* Panel containing selected sprite */
		final SpriteLoader imagePanel = new SpriteLoader();
		/* Draw green circle where mouse clicked on image */
		imagePanel.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {
				mouseX = e.getX();
				mouseY = e.getY();
				imagePanel.repaint();
			}
			public void mouseEntered(MouseEvent e) {
			}
			public void mouseExited(MouseEvent e) {
			}
			public void mousePressed(MouseEvent e) {
			}
			public void mouseReleased(MouseEvent e) {
			}
		});
		
		window = new JFrame(fileName);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setPreferredSize(new Dimension(width, height + 120));
		
		/* Panel containing gui */
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(width, 60));
		
		/* Assign key */
		JLabel label = new JLabel("Assign to key:");
		final JTextField key = new JTextField(1);
		/* Store last pressed key to use as hotkey later */
		key.addKeyListener(new KeyListener(){
			public void keyTyped(KeyEvent e){
				keyTyped = (e.getKeyChar() + "").toUpperCase().charAt(0);
			}
			public void keyPressed(KeyEvent e) {	
			}
			public void keyReleased(KeyEvent e) {				
			}
		});
		/* Check to make sure hotkey is valid and not registered */
		key.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String text = key.getText().toUpperCase();

				if(text.length() > 1)
					check.setText("Invalid key");
				else if(manager.getRegisteredKeyCodes().contains((int) keyTyped)){
					check.setText("Key already registered");
				}
				else {
					/* Register this hotkey to load the selected sprite */
					HotkeyManager.registerHotkey(new Action("Create Temp Unit"){
						public void execute() {
							try {
								Sprite[] sprites = {new Sprite(sprite, 1, mouseX, mouseY)};
								GameObject unit = new GameObject(Main.getGame().getPlayer(), sprites, null, 0, 0, 0, Direction.North, UnitShape.SHAPE_1x1, UnitClass.Building);
								Main.getGame().onClick(new PlaceAction(unit));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, keyTyped);
					imageSelected = true;
					Main.getMain().getTopLevelAncestor().setVisible(true);
					window.setVisible(false);
				}
			}
		});
		check = new JLabel("", JLabel.CENTER);
		check.setPreferredSize(new Dimension(width, 30));
		
		panel.add(label);
		panel.add(key);
		panel.add(check);
		
		window.add(imagePanel);
		window.add(panel, BorderLayout.PAGE_END);
		window.pack();
		window.setVisible(visible);
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(sprite, null, 0, 0);
		g2.setColor(Color.GREEN);
		g2.fillOval(mouseX, mouseY, 10, 10);
	}
	
	
}
