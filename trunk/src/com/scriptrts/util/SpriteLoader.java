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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
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

	/**
	 * Window displaying all of the swing components
	 */
	private static JFrame window;
	
	/**
	 * Input manager used by main game loop
	 */
	private static InputManager manager;
	
	/**
	 * Sprite helper to store sprite and unit shape information
	 */
	private static SpriteHelper spriteHelper;
	
	/**
	 * Current image displayed in the window
	 */
	private static BufferedImage image;
	
	/**
	 * Original image to conserve quality for scaling
	 */
	private static BufferedImage origImage;
	
	/**
	 * Sprite currently being modified (if animated) and added to unit's sprite array
	 */
	private static Sprite spriteToAdd;
	
	/**
	 * Mouse click location = backX and backY coordinate of unit's sprite
	 */
	private static int mouseX, mouseY;
	
	/**
	 * Scale to apply to all unit sprites
	 */
	private static double scale = 1;
	
	/**
	 * Direction the current unit sprite faces
	 */
	private static Direction direction = Direction.North;
	
	/**
	 * Unit's class which affects facing direction and number of allocated sprites
	 */
	private static UnitClass unitClass = UnitClass.Standard;
	
	/**
	 * Number of sprites allocated in unit's sprite array, depends on unit class
	 */
	private static int numberOfSprites = 8 * SpriteState.values().length;
	
	/**
	 * Current sprite state, affects position/index of added sprite in sprite array
	 */
	private static SpriteState spriteState = SpriteState.Idle;
	
	/**
	 * Whether or not the current sprite is animated
	 */
	private static boolean isAnimated = false;
	
	/**
	 * If animated, the number of frames to allocate in the animation
	 */
	private static int numberOfFrames = 1;
	
	/**
	 * The current frame number of the image to add to the animated sprite
	 */
	private static int frameNumber = 1;
	
	/**
	 * The number of frames added so far to the animated sprite
	 */
	private static int framesAdded = 0;
	
	/**
	 * The duration (in frames) of the current frame of the animated sprite
	 */
	private static int duration = 10;
	
	/**
	 * The file name of the currently loaded image
	 */
	private static String fileName;
	
	/**
	 * The file directory of the currently loaded image 
	 */
	private static String fileDirectory = "resource/";
	
	/**
	 * Width and height of the window
	 */
	private static int width, height;
	
	/**
	 * Whether or not the window is visible
	 */
	private static boolean visible;
	
	/**
	 * The last key typed in the hotkey text field, used to set and validate hotkeys 
	 */
	private static char keyTyped;

	/**
	 * Instantiates a new sprite loader
	 */
	public SpriteLoader(){
		super(true);
		promptForImage();
	}

	/**
	 * Initializes stuff, loads a sprite if needed, and finds the shape of a unit
	 * @param args
	 */
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
		spriteHelper = new SpriteHelper();
		UnitPainter unitPainter = Main.getGame().getUnitPainter();
		MapGrid grid = Main.getGame().getGameGrid();
		Viewport viewport = Main.getGame().getViewport();
		ClickAction clickAction = null;

		manager.registerKeyCode(KeyEvent.VK_BACK_SLASH);
		manager.registerKeyCode(KeyEvent.VK_ENTER);
		manager.registerKeyCode(KeyEvent.VK_ESCAPE);

		init();

		while(true){			
			handleInput(unitPainter, grid, viewport, clickAction);
			try{
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * All of the swing jank, and a couple of action listeners
	 */
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
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
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
				try {
					loadButton(status);
				} catch (IOException e1) {
					e1.printStackTrace();
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

		/* Unit class label */
		final JLabel unitClassLabel = new JLabel("Unit class:");

		/* Unit class select drop down menu */
		final JComboBox unitClassMenu = new JComboBox(UnitClass.values());
		unitClassMenu.setSelectedIndex(unitClass.ordinal());
		unitClassMenu.setPreferredSize(new Dimension(80, 20));
		unitClassMenu.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				selectUnitClass(unitClassMenu, directionMenu);
			}
		});

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
		final JTextField nFrames = new JTextField(2);
		nFrames.setText(numberOfFrames + "");
		nFrames.setEditable(isAnimated);
		numberOfFrames = Integer.valueOf(nFrames.getText());

		/* Frame number label */
		final JLabel frameNumberLabel = new JLabel("Frame number:");

		/* Frame number select menu */
		Integer[] ints = new Integer[numberOfFrames];
		for(int i = 0; i < numberOfFrames; i++)
			ints[i] = i + 1;
		final JSpinner frameNumberSelect = new JSpinner(new SpinnerListModel(ints));
		frameNumberSelect.setValue(frameNumber);
		frameNumberSelect.setPreferredSize(new Dimension(35, 20));
		frameNumberSelect.setEnabled(isAnimated);
		frameNumber = (Integer) frameNumberSelect.getValue();

		/* Add listener to change drop down menu when number of frames changes */
		nFrames.addFocusListener(new FocusListener(){
			public void focusLost(FocusEvent arg0) {
				changeNumberOfFrames(nFrames, frameNumberSelect);
			}
			public void focusGained(FocusEvent e) {
			}
		});

		frameNumberSelect.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				frameNumber = (Integer) frameNumberSelect.getValue();	
			}		
		});

		/* Animation duration label */
		final JLabel durationLabel = new JLabel("Duration:");
		
		/* Duration text box */
		final JTextField durationBox = new JTextField(2);
		if(isAnimated)
			durationBox.setText(duration + "");
		durationBox.setEditable(isAnimated);
		durationBox.addFocusListener(new FocusListener(){
			public void focusLost(FocusEvent e) {
				duration = Integer.valueOf(durationBox.getText());
			}
			public void focusGained(FocusEvent e) {}
		});
		
		
		/* Animated label */
		final JLabel animatedLabel = new JLabel("Animated:");

		/* Animated sprite check box */
		final JCheckBox animatedCheckBox = new JCheckBox();
		animatedCheckBox.setSelected(isAnimated);
		animatedCheckBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				checkAnimated(animatedCheckBox, nFrames, frameNumberSelect, durationBox);
			}
		});

		row3.add(spriteStateLabel);
		row3.add(spriteStateMenu);
		row3.add(animatedLabel);
		row3.add(animatedCheckBox);
		row3.add(nFramesLabel);
		row3.add(nFrames);
		row3.add(frameNumberLabel);
		row3.add(frameNumberSelect);


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
				validateHotkey(key, status);
			}
		});

		row4.add(durationLabel);
		row4.add(durationBox);
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

	/**
	 * Paints the loaded image on the screen and draws a green circle where the backX and backY
	 * (mouse clicked location) are
	 */
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(image, null, 0, 0);
		g2.setColor(Color.GREEN);
		g2.fillOval(mouseX, mouseY, 10, 10);
	}

	/**
	 * Prompts the user to select an image to load from the directory
	 */
	public void promptForImage(){
		/* Load the image from the file directory */
		JFileChooser browser = new JFileChooser(new File(fileDirectory));
		this.add(browser);

		/* Whether the user pressed "Ok" or "Cancel" */
		int returnValue = browser.showOpenDialog(this);
		File file = browser.getSelectedFile();

		/* If "Ok", load the image and set some variables */
		if(returnValue == JFileChooser.APPROVE_OPTION){
			try {
				fileName = file.toString();
				fileDirectory = browser.getCurrentDirectory().toString();
				loadImage(file.toString());
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
	
	
	/**
	 * Loads an image from the specified location, and sets the direction field if it can, i.e. if the
	 * image file name contains a direction
	 * @param file file name of image to load
	 * @throws IOException
	 */
	public static void loadImage(String file) throws IOException{
		origImage = ResourceManager.loadImage(file);
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
	}

	/**
	 * Handles the input once in the game, waits for backslash to add to unit's shape and enter to
	 * print out unit's shape
	 * @param unitPainter unit painter used by main game loop
	 * @param grid grid used by main game loop
	 * @param viewport viewport used by main game loop
	 * @param clickAction click action used by main game loop
	 * 
	 */
	public static void handleInput(UnitPainter unitPainter, MapGrid grid, Viewport viewport, ClickAction clickAction){
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
				System.out.println(generateUnitShape(spriteHelper.getUnitShape()));
			}
			spriteHelper = new SpriteHelper();
		}

		/* Load another sprite */
		if(manager.getKeyCodeFlag(KeyEvent.VK_ESCAPE)){
			init();
		}
	}

	/**
	 * Action listener for load button, adds sprites to unit's sprite array
	 * @param status status label passed in as parameter
	 * @throws IOException
	 */
	public static void loadButton(JLabel status) throws IOException{
		/* Determine how many spaces are allocated and where to put sprite */
		int index;
		if(unitClass == UnitClass.Standard)
			index = direction.ordinal() + 8 * spriteState.ordinal();
		else
			index = spriteState.ordinal();

		/* For an animated sprite, update the current frame */
		if(isAnimated){
			/* If this is the first frame being added create a new animated sprite instance */
			if(spriteToAdd == null){
				spriteToAdd = new AnimatedSprite(numberOfFrames, scale);
				int n = getNumeral(fileName);
				int indexOfN = fileName.lastIndexOf(n + "");
				/* Ask user whether or not to load the rest of the frames */
				if(numberOfFrames > 1 && n >= 0){
					int addAllFrames = JOptionPane.showOptionDialog(window, "Add rest of animation frames?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
					if(addAllFrames == JOptionPane.YES_OPTION){
						int oldFrameNumber = frameNumber;
						frameNumber = 1;
						/* Loads each frame individually */
						for(int i = 0; i < numberOfFrames; i++){
							fileName = fileName.substring(0, indexOfN) + i + fileName.substring(indexOfN + (n + "").length(), fileName.length());
							loadImage(fileName);
							if(unitClass == UnitClass.Standard)
								index = direction.ordinal() + 8 * spriteState.ordinal();
							else
								index = spriteState.ordinal();
							addAnimatedSpriteFrame(status, index);
							n = i;
							indexOfN = fileName.lastIndexOf(n + "");
							frameNumber++;
						}
						frameNumber = oldFrameNumber;
					}
					/* Otherwise just load this frame */
					else
						addAnimatedSpriteFrame(status, index);
				}
			}
			/* Otherwise just add this frame to the current animated sprite instance */
			else
				addAnimatedSpriteFrame(status, index);
		}
		/* For a regular sprite, just add the sprite */
		else {
			spriteToAdd = new Sprite(image, scale, mouseX, mouseY);
			spriteHelper.addImageFile(fileName, index, 1);
		}
		/* If the sprite is static or has all of its frames loaded, add the sprite to the unit's sprite array */
		if(!(spriteToAdd instanceof AnimatedSprite) || framesAdded == numberOfFrames){
			spriteHelper.addSprite(spriteToAdd, index);
			spriteToAdd = null;
			framesAdded = 0;
			status.setText("Sprite loaded");
		}
	}

	/**
	 * Action listener for unit class drop down menu, updates the number of allocated sprites and
	 * enables/disables direction drop down menu since buildings and terrain objects always face north
	 * @param unitClassMenu unit class drop down menu passed in as parameter
	 * @param directionMenu direction drop down menu passed in as parameter
	 */
	public static void selectUnitClass(JComboBox unitClassMenu, JComboBox directionMenu){
		UnitClass oldUnitClass = unitClass;
		unitClass = (UnitClass) unitClassMenu.getSelectedItem();
		if(unitClass != oldUnitClass){
			if(unitClass == UnitClass.Standard){
				numberOfSprites = 8 * SpriteState.values().length;
				spriteHelper.setNumberOfSprites(numberOfSprites);
				directionMenu.setEnabled(true);
			}
			else{
				numberOfSprites = SpriteState.values().length;
				spriteHelper.setNumberOfSprites(numberOfSprites);
				direction = Direction.North;
				directionMenu.setSelectedItem(Direction.North);
				directionMenu.setEnabled(false);
			}
		}
	}

	/**
	 * Focus listener for number of frames text field, updates the current frame number to make sure
	 * it is within range
	 * @param nFrames number of frames text field passed in as parameter
	 * @param frameNumberSelect frame number selection field passed in as parameter
	 */
	public static void changeNumberOfFrames(JTextField nFrames, JSpinner frameNumberSelect){
		numberOfFrames = Integer.valueOf(nFrames.getText());
		int selected = (Integer) frameNumberSelect.getValue();
		Integer[] ints = new Integer[numberOfFrames];
		for(int i = 0; i < numberOfFrames; i++)
			ints[i] = i + 1;
		frameNumberSelect.setModel(new SpinnerListModel(ints));
		if(selected > numberOfFrames)
			frameNumberSelect.setValue(numberOfFrames);
		frameNumber = (Integer) frameNumberSelect.getValue();
	}

	/**
	 * Action listener for the animated check box, enables/disables fields related to animated sprites,
	 * i.e. number of frames, current frame number, and duration
	 * @param animatedCheckBox animated check box the action listener is for
	 * @param nFrames number of frames text field passed in as parameter
	 * @param frameNumberSelect frame number selection field passed in as parameter
	 * @param durationBox duration text field passed in as parameter
	 */
	public static void checkAnimated(JCheckBox animatedCheckBox, JTextField nFrames, JSpinner frameNumberSelect, JTextField durationBox){
		if(animatedCheckBox.isSelected()){
			isAnimated = true;
			nFrames.setEditable(true);
			frameNumberSelect.setEnabled(true);
			durationBox.setText(duration + "");
			durationBox.setEditable(true);
		} else{
			isAnimated = false;
			nFrames.setText("1");
			changeNumberOfFrames(nFrames, frameNumberSelect);
			nFrames.setEditable(false);
			frameNumberSelect.setEnabled(false);
			durationBox.setText("");
			durationBox.setEditable(false);
		}
	}

	/**
	 * Check to make sure hotkey is valid (1 character) and not already assigned
	 * @param key text field containing hotkey to check
	 * @param status status label passed in as parameter
	 */
	public static void validateHotkey(JTextField key, JLabel status){
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
						GameObject unit = new GameObject(Main.getGame().getPlayer(), spriteHelper.getSprites(), null, unitSpeed, 0, 0, direction, UnitShape.SHAPE_1x1, unitClass);
						Main.getGame().onClick(new PlaceAction(unit));
						generateSpriteSheet(unit);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, keyTyped);
			window.setVisible(false);
		}
	}

	/**
	 * Add a frame to an animated sprite using the current frame number, image, duration, and backX and backY locations 
	 * @param status status label passed in as parameter
	 * @param index index that uniquely identifies the direction and sprite state for this sprite,
	 * given by direction.ordinal() + 8 * spriteState.ordinal() for standard units, or spriteState.ordinal()
	 */
	public static void addAnimatedSpriteFrame(JLabel status, int index){
		if(((AnimatedSprite) spriteToAdd).getFrameImage(frameNumber) == null){
			((AnimatedSprite) spriteToAdd).addFrame(frameNumber, image, duration, mouseX, mouseY);
			framesAdded++;
			status.setText("Loaded " + framesAdded + " frames so far");
		}
		else{
			((AnimatedSprite) spriteToAdd).replaceFrame(frameNumber, image, duration, mouseX, mouseY);
			status.setText("Replaced frame " + frameNumber);
		}
		spriteHelper.addImageFile(fileName, index, frameNumber);
	}
	
	/**
	 * Generate a unit's sprite sheet, currently writes to "output.txt"
	 * @param unit unit to write sprite sheet for
	 * @throws IOException
	 */
	public static void generateSpriteSheet(GameObject unit) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter("output.txt"));
		Sprite[] sprites = unit.getSprites();
		bw.write(numberOfSprites + "");
		bw.newLine();
		bw.write(sprites[0].getScale() + "");
		bw.newLine();
		for(int i = 0; i < sprites.length; i++){
			Sprite s = sprites[i];
			writeSprite(bw, s, i);
		}
		bw.close();
	}

	/**
	 * Write a single sprite in the unit's sprite array to the unit's sprite sheet
	 * @param bw buffered writer to writer with
	 * @param s sprite to write
	 * @param index index that uniquely identifies the direction and sprite state for this sprite,
	 * given by direction.ordinal() + 8 * spriteState.ordinal() for standard units, or spriteState.ordinal()
	 * @throws IOException
	 */
	public static void writeSprite(BufferedWriter bw, Sprite s, int index) throws IOException{
		if(s instanceof AnimatedSprite){
			bw.write("ANIMATED");
			bw.newLine();
			int nFrames = ((AnimatedSprite) s).getNumberFrames();
			bw.write(nFrames + "");
			bw.newLine();
			for(int i = 1; i <= nFrames; i++){
				int duration = ((AnimatedSprite) s).getFrameDuration(i);
				int backX = ((AnimatedSprite) s).getFrameBackX(i);
				int backY = ((AnimatedSprite) s).getFrameBackY(i);
				bw.write(duration + "");
				bw.newLine();
				bw.write(backX + " " + backY);
				bw.newLine();
				bw.write(getFileName((String) (spriteHelper.getImageFiles()[index].get(i - 1))));
				bw.newLine();
			}
		} else if (s != null){
			bw.write("STATIC");
			bw.newLine();
			int backX = s.getSpriteBackX();
			int backY = s.getSpriteBackY();
			bw.write(backX + " " + backY);
			bw.newLine();
			bw.write(getFileName((String) (spriteHelper.getImageFiles()[index].get(0))));
			bw.newLine();
		} else {
			bw.write("NULL");
			bw.newLine();
		}
		bw.newLine();
		bw.flush();
	}

	/**
	 * Trim a file name of the leading directory info (up to "resource/")
	 * and convert the file name with backslashes to one with slashes
	 * @param s file name to convert
	 * @return new file name used in sprite sheet
	 */
	public static String getFileName(String s){
		int startIndex = s.indexOf("resource") + 9;
		if(startIndex <= 8)
			return null;
		String file = "";
		for(int i = startIndex; i < s.length(); i++){
			if(s.charAt(i) == '\\')
				file = file + "/";
			else
				file = file + s.charAt(i);
		}
		return file;
	}

	/**
	 * Generate the code for a unit shape
	 * @param unitShape array list of points in the current unit shape
	 * @return code format of the points in the unit shape, i.e. new Point[]{p1, p2, ...}
	 */
	public static String generateUnitShape(ArrayList<Point> unitShape){
		String s = "new Point[]{";
		for(int i = 0; i < unitShape.size(); i++){
			Point p = unitShape.get(i);
			s = s + "new Point(" + p.x + "," + p.y + "), "; 
		}
		s = s.substring(0, s.length() - 2);
		s = s + "}";
		return s;
	}
	
	/**
	 * Get the frame number of an image used for an animated sprite, i.e. DroneEast5.png corresponds to frame 6
	 * @param s file name string to check
	 * @return frame number of image
	 */
	public static int getNumeral(String s){
		for(int i = s.length() - 1; i >= 0; i--)
			for(int j = 0; j <= numberOfFrames - 1; j++){
				int digits = (j + "").length();
				try{
					if(Integer.valueOf(s.substring(i, i + digits)) == j)
						return j;
				} catch (Exception e){}
			}
		return -1;
	}
}