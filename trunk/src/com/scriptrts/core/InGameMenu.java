package com.scriptrts.core;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The in-game menu that has important things like saving, loading, and
 * accessing the game settings and probably main menu of the game if that ever
 * comes up.
 * 
 * @author Arsy
 * 
 */
public class InGameMenu extends JPanel {
	/**
	 * doesn't do anything yet
	 */
	public JButton save;

	/**
	 * doesn't do anything yet
	 */
	public JButton load;

	/**
	 * Closes the menu (does nothing unless a listener is set somewhere else
	 * after the Popup is made)
	 */
	public JButton close;

	/**
	 * doesn't do anything yet
	 */
	public JButton settings;

	public InGameMenu() {
		super();

		/* Possibly extract to be arguments */
		setSize(400,300);
		setPreferredSize(getSize());
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		save = new JButton("Save");
		load = new JButton("Load");
		settings = new JButton("Settings");
		close = new JButton("Close");

		save.setAlignmentX(CENTER_ALIGNMENT);
		load.setAlignmentX(CENTER_ALIGNMENT);
		settings.setAlignmentX(CENTER_ALIGNMENT);
		close.setAlignmentX(CENTER_ALIGNMENT);
		
		add(save);
		add(load);
		add(settings);
		add(close);

		add(new JLabel("Only 'Close' does stuff... we can change that"));
		add(new JLabel("Also this can 'easily' be made waaay more pretty"));
		

	}
}
