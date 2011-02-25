import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

public class ConsoleTester extends JFrame implements KeyListener {

	private static final int WIDTH = 800, HEIGHT = 600;
	private MyPanel panel;
	private Timer timer;

	private boolean isConsoleDown;

	public static void main(String... args) {
		ConsoleTester test = new ConsoleTester();
	}

	public ConsoleTester() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		pack();
		setVisible(true);
		panel = new MyPanel();
		add(panel);
		addKeyListener(this);
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				repaint();
			}
		}, 0, 75);	
	}

	private class MyPanel extends JPanel {
		public void paint(Graphics g) {
			g.setColor(Color.white);
			g.fillRect(0, 0, getWidth(), getHeight());
			if(isConsoleDown) {
				g.setColor(new Color(0, 0, 0, 175));
				g.fillRect(0, 0, getWidth(), getHeight() / 5);
			}
		}
	}

	public void keyPressed(KeyEvent evt) {
		if(evt.getKeyCode() == KeyEvent.VK_BACK_QUOTE)
			isConsoleDown = ! isConsoleDown;
	}

	public void keyReleased(KeyEvent evt) {

	}

	public void keyTyped(KeyEvent evt) {

	}
}
