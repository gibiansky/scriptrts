/**
 * INSTRUCTIONS:
 * Run SpriteLoader.java with debug on
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
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.scriptrts.core.ClickAction;
import com.scriptrts.core.Main;
import com.scriptrts.core.PlaceAction;
import com.scriptrts.core.ui.Action;
import com.scriptrts.core.ui.HotkeyManager;
import com.scriptrts.core.ui.InputManager;
import com.scriptrts.core.ui.UnitPainter;
import com.scriptrts.core.ui.Viewport;
import com.scriptrts.game.AnimatedSprite;
import com.scriptrts.game.Direction;
import com.scriptrts.game.GameObject;
import com.scriptrts.game.MapGrid;
import com.scriptrts.game.Sprite;
import com.scriptrts.game.SpriteState;
import com.scriptrts.game.UnitClass;
import com.scriptrts.game.UnitShape;

public class SpriteLoader extends JPanel{

	private static JFrame window;
	private static InputManager manager;
	private static BufferedImage image;
	private static BufferedImage origImage;
	private static Sprite spriteToAdd;
	private static Sprite[] sprites = new Sprite[8];
	private static int mouseX, mouseY;
	private static double scale = 1;
	private static Direction direction = Direction.North;
	private static UnitClass unitClass = UnitClass.Standard;
	private static SpriteState spriteState = SpriteState.Idle;
	private static boolean isAnimated = false;
	private static int n = 1;
	private static int frameNumber = 1;
	private static int framesAdded = 0;
	private static String fileName;
	private static String fileDirectory = "resource/";
	private static int width, height;
	private static boolean visible;
	private static char keyTyped;

	public SpriteLoader(){
		super(true);
		/* Load the image from the file directory */
		JFileChooser browser = new JFileChooser(new File(fileDirectory));
		this.add(browser);

		/* Whether the user pressed "Ok" or "Cancel" */
		int returnValue = browser.showOpenDialog(this);
		File file = browser.getSelectedFile();

		/* If "Ok", load the image and set some variables */
		if(returnValue == JFileChooser.APPROVE_OPTION){
			try {
				origImage = ResourceManager.loadImage(file.toString());
				fileName = file.toString();
				fileDirectory = browser.getCurrentDirectory().toString();
				Direction[] directions = {Direction.Northeast, Direction.Northwest, Direction.Southeast, Direction.Southwest, Direction.North, Direction.South, Direction.East, Direction.West};
				for(Direction d : directions){
					if(fileName.toLowerCase().indexOf(d.toString().toLowerCase()) != -1){
						direction = d;
						break;
					}
				}
				width = Math.max(origImage.getWidth(), 600);
				height = Math.max(origImage.getHeight(), 200);
				image = origImage;
				visible = true;
			} catch (IOException e) {
				System.out.println("File not found.");
				visible = false;
				this.setVisible(visible);
			}
		}
		/* Otherwise quit out of the selection screen */
		else {
			visible = false;
			this.setVisible(visible);
		}
	}

	public static void main(final String[] args){
		/* Start game */		
		Thread thread = new Thread(){
			public void run(){
				Main.main(args);
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
		/* Panel containing the image of the selected sprite */
		final SpriteLoader imagePanel = new SpriteLoader();

		/* Set window dimensions after loading the image */
		window = new JFrame(fileName);
		window.setPreferredSize(new Dimension(width, height + 200));

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

		/* Panel containing all of the gui */
		final JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.setPreferredSize(new Dimension(width, 120));
		
		/* Status label */
		final JLabel statusLabel = new JLabel("Status:");
		
		/* Error message label */
		final JLabel status = new JLabel();
		
		/* Panel containing the file loading stuff */
		final JPanel row1 = new JPanel();

		/* File label */
		final JLabel fileLabel = new JLabel("File:");

		/* File name text box */
		final JTextField fileSource = new JTextField(fileName);
		fileSource.setPreferredSize(new Dimension(300, 20));

		/* Load image button */
		final JButton loadButton = new JButton("Load");
		loadButton.setPreferredSize(new Dimension(90, 20));
		loadButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(isAnimated){
					if(spriteToAdd == null)
						spriteToAdd = new AnimatedSprite(n, scale);
					if(spriteToAdd instanceof AnimatedSprite){
						if(((AnimatedSprite) spriteToAdd).getFrameImage(frameNumber) == null){
							((AnimatedSprite) spriteToAdd).addFrame(frameNumber, image, 2, mouseX, mouseY);
							framesAdded++;
							status.setText("Loaded " + framesAdded + " frames so far");
						}
						else{
							((AnimatedSprite) spriteToAdd).replaceFrame(frameNumber, image, 2, mouseX, mouseY);
							status.setText("Replaced frame " + frameNumber);
						}
						
					}
				} else {
					spriteToAdd = new Sprite(image, scale, mouseX, mouseY);
				}
				if(!(spriteToAdd instanceof AnimatedSprite) || framesAdded == n){
					sprites[direction.ordinal() + 8 * spriteState.ordinal()] = spriteToAdd;
					spriteToAdd = null;
					framesAdded = 0;
					status.setText("Sprite loaded");
				}
			}
		});

		/* Browse for new image button */
		final JButton browseButton = new JButton("Browse");
		browseButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				window.setEnabled(false);
				window.setVisible(false);
				init();
			}
		});
		browseButton.setPreferredSize(new Dimension(90, 20));

		row1.add(fileLabel);
		row1.add(fileSource);
		row1.add(loadButton);
		row1.add(browseButton);


		/* Panel containing the image scaling stuff */
		final JPanel row2 = new JPanel();

		/* Scale label */
		final JLabel scaleLabel = new JLabel("Scale:");

		/* Scale image sliding bar */
		final JLabel sliderLabel = new JLabel();
		final JSlider slider = new JSlider(1, 20, 10);
		slider.setValue((int) (scale * 10));
		slider.setPreferredSize(new Dimension(150, 20));
		slider.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				sliderLabel.setText((double)(slider.getValue())/10 + "");
				scale = Double.valueOf(sliderLabel.getText());
			}
		});
		sliderLabel.setText((double)(slider.getValue())/10 + "");
		scale = Double.valueOf(sliderLabel.getText());

		/* Direction label */
		final JLabel directionLabel = new JLabel("Direction:");

		/* Direction select drop down menu */
		final JComboBox directionMenu = new JComboBox(Direction.values());
		directionMenu.setSelectedIndex(direction.ordinal());
		directionMenu.setPreferredSize(new Dimension(90, 20));
		directionMenu.setEnabled(unitClass == UnitClass.Standard);
		directionMenu.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				direction = (Direction) directionMenu.getSelectedItem();
			}
		});
		direction = (Direction) directionMenu.getSelectedItem();
		
		/* Unit class label */
		final JLabel unitClassLabel = new JLabel("Unit class:");
		
		/* Unit class select drop down menu */
		final JComboBox unitClassMenu = new JComboBox(UnitClass.values());
		unitClassMenu.setSelectedIndex(unitClass.ordinal());
		unitClassMenu.setPreferredSize(new Dimension(80, 20));
		unitClassMenu.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				UnitClass oldUnitClass = unitClass;
				unitClass = (UnitClass) unitClassMenu.getSelectedItem();
				if(unitClass != oldUnitClass){
					if(unitClass == UnitClass.Standard){
						sprites = new Sprite[8 * SpriteState.values().length];
						directionMenu.setEnabled(true);
					}
					else{
						sprites = new Sprite[1 * SpriteState.values().length];
						direction = Direction.North;
						directionMenu.setSelectedItem(Direction.North);
						directionMenu.setEnabled(false);
					}
				}
			}
		});
		unitClass = (UnitClass) unitClassMenu.getSelectedItem();

		row2.add(scaleLabel);
		row2.add(sliderLabel);
		row2.add(slider);
		row2.add(directionLabel);
		row2.add(directionMenu);
		row2.add(unitClassLabel);
		row2.add(unitClassMenu);


		/* Panel containing the sprite information */
		final JPanel row3 = new JPanel();
		
		/* Sprite state label */
		final JLabel spriteStateLabel = new JLabel("Sprite state:");
		
		/* Sprite state select drop down menu */
		final JComboBox spriteStateMenu = new JComboBox(SpriteState.values());
		spriteStateMenu.setSelectedIndex(spriteState.ordinal());
		spriteStateMenu.setPreferredSize(new Dimension(70, 20));
		spriteStateMenu.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				spriteState = (SpriteState) spriteStateMenu.getSelectedItem();
			}
		});
		spriteState = (SpriteState) spriteStateMenu.getSelectedItem();
		
		/* Number of frames label */
		final JLabel nFramesLabel = new JLabel("Number of Frames:");

		/* Number of frames text box */
		final JTextField nFrames = new JTextField("1", 2);
		nFrames.setText(n + "");
		nFrames.setEditable(isAnimated);
		n = Integer.valueOf(nFrames.getText());
		
		/* Frame number label */
		final JLabel frameNumberLabel = new JLabel("Frame number:");
		
		/* Frame number select drop down menu */
		Integer[] ints = new Integer[Integer.valueOf(nFrames.getText())];
		for(int i = 0; i < ints.length; i++)
			ints[i] = i + 1;
		final JComboBox frameNumberMenu = new JComboBox(ints);
		frameNumberMenu.setSelectedIndex(frameNumber - 1);
		frameNumberMenu.setEnabled(isAnimated);
		frameNumber = (Integer) frameNumberMenu.getSelectedItem();
		
		/* Add listener to change drop down menu when number of frames changes */
		nFrames.addFocusListener(new FocusListener(){
			public void focusLost(FocusEvent arg0) {
				n = Integer.valueOf(nFrames.getText());
				
				int oldN = frameNumberMenu.getItemCount();
				int selected = (Integer) frameNumberMenu.getSelectedItem();
				if(n > oldN){
					for(int i = oldN + 1; i <= n; i++)
						frameNumberMenu.addItem(i);
				} else if(n < oldN){
					for(int i = n + 1; i <= oldN; i++)
						frameNumberMenu.removeItem(i);
				}
				if(selected > n)
					frameNumberMenu.setSelectedItem(n);
				frameNumber = (Integer) frameNumberMenu.getSelectedItem();
			}
			public void focusGained(FocusEvent e) {
			}
		});
		
		frameNumberMenu.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				frameNumber = (Integer) frameNumberMenu.getSelectedItem();
			}
			
		});

		/* Animated label */
		final JLabel animatedLabel = new JLabel("Animated:");
		
		/* Animated sprite check box */
		final JCheckBox animatedCheckBox = new JCheckBox();
		animatedCheckBox.setSelected(isAnimated);
		animatedCheckBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(animatedCheckBox.isSelected()){
					isAnimated = true;
					nFrames.setEditable(true);
					frameNumberMenu.setEnabled(true);
				} else{
					isAnimated = false;
					nFrames.setEditable(false);
					frameNumberMenu.setEnabled(false);
				}
			}
		});
		
		row3.add(spriteStateLabel);
		row3.add(spriteStateMenu);
		row3.add(animatedLabel);
		row3.add(animatedCheckBox);
		row3.add(nFramesLabel);
		row3.add(nFrames);
		row3.add(frameNumberLabel);
		row3.add(frameNumberMenu);
		
		
		/* Panel containing the hotkey stuff */
		JPanel row4 = new JPanel();
		
		/* Hotkey assigning text field */
		final JLabel keyLabel = new JLabel("Assign to key:");

		/* Hotkey text box */
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
					status.setText("Invalid key");
				else if(manager.getRegisteredKeyCodes().contains((int) keyTyped)){
					status.setText("Key already registered");
				}
				else {
					/* Register this hotkey to load the selected sprite */
					HotkeyManager.registerHotkey(new Action("Create Temp Unit"){
						public void execute() {
							try {
								int unitSpeed;
								if(unitClass == UnitClass.Standard)
									unitSpeed = 5;
								else
									unitSpeed = 0;
								GameObject unit = new GameObject(Main.getGame().getPlayer(), sprites, null, unitSpeed, 0, 0, direction, UnitShape.SHAPE_1x1, unitClass);
								Main.getGame().onClick(new PlaceAction(unit));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, keyTyped);
					window.setVisible(false);
				}
			}
		});

		row4.add(keyLabel);
		row4.add(key);
		row4.add(statusLabel);
		row4.add(status);

		/* Add items to panel */
		panel.add(row1);
		panel.add(row2);
		panel.add(row3);
		panel.add(row4);

		/* Add panels to window */
		window.add(imagePanel);
		window.add(panel, BorderLayout.SOUTH);
		window.pack();
		window.setVisible(visible);
	}

	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(image, null, 0, 0);
		g2.setColor(Color.GREEN);
		g2.fillOval(mouseX, mouseY, 10, 10);
	}
}
