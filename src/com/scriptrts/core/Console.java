package com.scriptrts.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.scriptrts.script.Script;

/**
 * In-game terminal implemented as a Swing component. 
 *
 * The terminal consists of a text field and a text area. The text area is the output, while the 
 * text field is the input. When the user is done inputting, the console evaluates the input as
 * Python code and appends output to the text area.
 */
public class Console extends JPanel {
    /**
     * The list of previously-entered commands.
     */
    private ArrayList<String> history;

    /**
     * Font used by the text area and text field.
     */
    private static Font font = new Font("Monospaced", Font.PLAIN, 14);

    /**
     * Character size calculated in order to resize the console to fit the window.
     */
    private static int charWidth = -1;
    private static int charHeight = -1;

    /**
     * Input text field.
     */
    private JTextField console;

    /**
     * Scroll pane that houses the output area and provides a vertical scroll bar.
     */
    private JScrollPane areaScrollPane;

    /**
     * Output text area, updated whenever a command is run.
     */
    private JTextArea output;

    /**
     * How many rows the console output text area should be when there is nothing in it. Determined by
     * what portion of the window height it should take up, and how high the window is.
     */
    private int originalNumRows = -1;

    /**
     * Boolean representing whether or not a command is currently being interpreted. If it is true, 
     * commands entered will not be interpreted and will be ignored until the current command is done.
     */
    private boolean running = false;

    /**
     * String storing the current output residing in the output text area.
     */
    private String terminalText = "";

    /**
     * Boolean representing whether or not the previous command was interrupted. This is needed because
     * a command after being run updates the output; if it was interrupted, it should add some sort of
     * notification, along the lines of "KeyboardInterrupt" after its normal output.
     */
    private boolean interrupted = false;

    /**
     * Where in the history the user currently is. The up and down arrow keys manipulate which element of the
     * history to display, and running a command resets it to the last command in the history.
     */
    private int currentHistoryLocation = -1;


    /**
     * The accumulated command code, includes several lines when there is a multi-line input.
     */
    private String totalCommandText = "";
    
    /**
     * Returns whether the character size has been calculated. If it hasn't, then the console should be calibrated
     * with the help of a Graphics object representing the current graphics configuration.
     */
    public static boolean calibrated(){
        return !(charWidth < 0 || charHeight < 0);
    }

    /**
     * Calibrate the console font for the current graphics configuration. Must be called before the console is used.
     */
    public static void calibrateFont(Graphics graphics){
        FontMetrics metrics = graphics.getFontMetrics(font);
        charWidth = metrics.charWidth('A');
        charHeight = metrics.getHeight();
    }

    /**
     * Create a new console.
     *
     * @param consoleHeight the height to fit the console into
     * @param screenWidth the desired width of the console
     */
    public Console(int consoleHeight, int screenWidth){
        /* Use double buffering for this JPanel */
        super(true);

        /* Store history in a a list */
        history = new ArrayList<String>();

        /* Transparent background */
        setBackground(new Color(0, 0, 0, 0));

        /* Calculate rows in the output */
        int rows = consoleHeight / charHeight, columns = screenWidth / charWidth + 1;
        originalNumRows = rows;

        /* Create the input text field */
        Color color = new Color(0, 0, 0, 120);
        Color gray = new Color(220, 220, 220, 255);
        console = new JTextField(columns);
        console.setFont(font);
        console.setBackground(color);
        console.setForeground(gray);
        console.setCaretColor(Color.white);

        /* Create the output text area */
        output = new JTextArea(rows, columns);
        output.setLineWrap(true);
        output.setFont(font);
        output.setEditable(false);
        output.setBackground(new Color(0, 0, 0, 0));
        output.setForeground(gray);

        /* Add the output to a scroll pane, so we can scroll through output */
        areaScrollPane = new JScrollPane(output);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Dimension dim = output.getPreferredSize();
        dim.height += 5;
        areaScrollPane.setPreferredSize(dim);
        areaScrollPane.setBackground(color);

        /* Use absolute positioning */
        setLayout(null);
        add(areaScrollPane);
        add(console);

        /* Position the input and output elements */
        Dimension oDim = areaScrollPane.getPreferredSize();
        areaScrollPane.setBounds(0, 0, screenWidth, oDim.height);
        Dimension cDim = console.getPreferredSize();
        console.setBounds(0, oDim.height, cDim.width, cDim.height);

        /* When we press enter, try to run the command */
        console.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                runCommand();
            }
        });

        /* Listen for other events */
        console.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent ev){
                /* History by pressing up and down */
                if(ev.getKeyCode() == KeyEvent.VK_UP) {
                    historyUp();
                }
                else if(ev.getKeyCode() == KeyEvent.VK_DOWN) {
                    historyDown();
                }

                /* Clearing with Ctrl-L */
                else if(ev.getKeyCode() == KeyEvent.VK_L){
                    if(ev.isControlDown())
                        clearConsole();
                }

                /* Moving cursor to start of line with Ctrl-w */
                else if(ev.getKeyCode() == KeyEvent.VK_W){
                    if(ev.isControlDown())
                        cursorToStart();
                }

                /* Moving cursor to end of line with Ctrl-e */
                else if(ev.getKeyCode() == KeyEvent.VK_E){
                    if(ev.isControlDown())
                        cursorToEnd();
                }

                /* Stopping execution with Ctrl-C */
                else if(ev.getKeyCode() == KeyEvent.VK_Z){
                    if(ev.isControlDown())
                        stopExecution();
                }

                /* Deleting remainder of line with Ctrl-k */
                else if(ev.getKeyCode() == KeyEvent.VK_K){
                    if(ev.isControlDown())
                        deleteLineRemainder();
                }
            }
        });
    }

    /**
     * Stop the execution of the current command.
     */
    private void stopExecution(){
        /* We have a builtin interrupt in the core module */
        Script.exec("core.interrupt()", null);

        /* Stop the program, notify that its interrupted */
        running = false;
        interrupted = true;
    }

    /**
     * Move the caret in the input area to the start.
     */
    private void cursorToStart(){
        console.setCaretPosition(0);
    }

    /**
     * Move the caret in the input area to the end.
     */
    private void cursorToEnd(){
        console.setCaretPosition(console.getText().length());
    }

    /** 
     * Delete everything after the caret in the input area.
     */
    private void deleteLineRemainder(){
        int loc = console.getCaretPosition();
        String substr = console.getText().substring(0, loc);
        console.setText(substr);
    }

    /**
     * Add a key listener to the console.
     *
     * @param list the key listener to add.
     */
    public void addKeyListener(KeyListener list){
        /* Add the key listener to the JPanel, the JTextArea, and JTextField */
        super.addKeyListener(list);
        console.addKeyListener(list);
        output.addKeyListener(list);
    }

    /**
     * Tell this component to request focus in its parent window.
     */
    public boolean requestFocusInWindow(){
        /* We want the cursor to work, so have the input area request focus */
        return console.requestFocusInWindow();
    }

    /**
     * Check whether this component has focus.
     *
     * @return true if the component has focus, false otherwise.
     */
    public boolean hasFocus(){
        /* Return true if any part of this component has focus */
        return console.hasFocus() || output.hasFocus() || super.hasFocus();
    }

    /**
     * Clear the console.
     */
    private void clearConsole(){
        output.setRows(originalNumRows);
        output.setText("");
        terminalText = "";
    }

    /**
     * Move one element up (earlier) in the history.
     */
    private void historyUp(){
        /* Only go up in history if we're not at the very start */
        if(currentHistoryLocation > 0){
            currentHistoryLocation--;
            console.setText(getTextFromHistory());
        }
    }

    /**
     * Move one element down (later) in the history.
     */
    private void historyDown(){
        /* Don't go beyond last element in history */
        if(currentHistoryLocation < history.size()){
            currentHistoryLocation++;
            console.setText(getTextFromHistory());
        }
    }

    /**
     * Get the text at the current history location.
     *
     * @return a string with a command from history, or "" if we're not using history
     */
    private String getTextFromHistory(){
        int index = currentHistoryLocation;
        if(index >= 0 && index < history.size())
            return history.get(index);
        else
            return "";
    }

    /**
     * Update the size of the console. This method is called whenever the screen is resized.
     *
     * @param consoleHeight the new desired height of the console
     * @param screenWidth the new desired width of the console
     */
    public void updateSize(int consoleHeight, int screenWidth){
        /* Recalculate rows and colums */
        int rows = consoleHeight / charHeight, columns = screenWidth / charWidth + 1;
        originalNumRows = rows;

        /* Resize elements */
        console.setColumns(columns);
        output.setColumns(columns);
        output.setRows(rows);

        /* Recalculate absolute sizes and positions */
        Dimension dim = output.getPreferredSize();
        dim.height += 5;
        areaScrollPane.setPreferredSize(dim);

        Dimension oDim = areaScrollPane.getPreferredSize();
        areaScrollPane.setBounds(0, 0, screenWidth, oDim.height);
        Dimension cDim = console.getPreferredSize();
        console.setBounds(0, oDim.height, cDim.width, cDim.height);
    }

    /**
     * Run a command and display its output.
     */
    private void runCommand(){
        /* Don't allow running commands while others are still running */
        if(running)
            return;

        /* Get the command and clear the input area */
        String commandText = console.getText();
        console.setText("");

        /* Find out how many tabs to put in */
        int tabsForNextLine = getNextLineIndents(commandText);

        /* Add the command header to console if this is the first line of a command */
        if(totalCommandText.equals("")) {
            terminalText += ">>>";

            if(tabsForNextLine == 0)
                terminalText += " ";
            else
                terminalText += "\n";
        }

        /* Add the command itself */
        terminalText +=  commandText + "\n";

        /* Store the line for later execution */
        totalCommandText += commandText + "\n";

        /* Add the command to history if it isn't whitespace, and isn't a repeat of the previous command */
        if(commandText.trim().length() != 0){
            if(history.isEmpty() || !commandText.equals(history.get(history.size() - 1)))
                history.add(commandText);
            currentHistoryLocation = history.size() - 1;
        }

        /* Update the output area */
        output.setText(terminalText);

        /* If this is a finished command, execute it */
        if(tabsForNextLine == 0){
            /* Start a new thread for the python command so this doesn't stop the game */
            new Thread(new Runnable(){
                public void run(){
                    /* Notify the rest of the program that a command is running */
                    running = true;
                    
                    /* Create a string writer initialized to the current console output */
                    String originalText = output.getText();
                    final StringWriter outputter = new StringWriter();
                    outputter.write(originalText);

                    /* Create yet another thread that does the actual command */
                    Thread interpreter = new Thread(new Runnable(){
                        public void run(){
                            /* Run the command, appending output to the string writer created above */
                            Script.exec(totalCommandText, outputter);

                            /* Once we're done, store the total output and nofity the program that this command is done */
                            terminalText = outputter.toString();
                            output.setText(terminalText);

                            /* Scroll to end of output */
                            output.setCaretPosition(output.getText().length());

                            running = false;
                        }
                    });

                    /* Don't let the interpreter lock up the rest of the game */
                    interpreter.setPriority(Thread.MIN_PRIORITY);
                    interpreter.start();

                    /* While a command is running, keep the output area updating */
                    while(running){
                        /* Update output */
                        terminalText = outputter.toString();
                        output.setText(terminalText);

                        /* Scroll to end of output */
                        output.setCaretPosition(output.getText().length());

                        /* If it's still running, wait a bit before updating the output again */
                        if(running)
                            try {
                                Thread.currentThread().sleep(300);
                            } catch (Exception e) { 
                                e.printStackTrace(); 
                            }
                    }

                    /* After we're done running, clear the current command */
                    totalCommandText = "";

                    /* After we're done running, update the output a final time */
                    terminalText = outputter.toString();

                    /* If we were interrupted, say so by adding an error message to the end */
                    if(interrupted){
                        terminalText += "KeyboardInterrupt\n";
                        interrupted = false;
                    }

                    /* Update output, scroll to the end */
                    output.setText(terminalText);
                    output.setCaretPosition(output.getText().length());
                }
            }).start();
        } 

        /* Add tabs to the next line */
        String tabs = "";
        for(int i = 0; i < tabsForNextLine; i++)
            tabs += "\t";
        console.setText(tabs);

        /* Set console history */
        currentHistoryLocation = history.size();
    }

    /**
     * Given a line of python code, calculate how many indents the next line should have. This essentially counts the number
     * of tabs at the start of the line, and adds one if there is a semicolon at the end of the line.
     *
     * @return number of tabs to add for next line
     */
    private int getNextLineIndents(String currentLine){
        /* If this is a non-indented line, the next line is non-indented (end of input) */
        if(!currentLine.startsWith("\t") && !currentLine.contains(":"))
            return 0;

        /* If the previous line is just whitespace, that signifies end of input so the next line is a normal, unindented line */
        if(currentLine.trim().length() == 0)
            return 0;

        /* Count number of tabs and insert that many at least */
        int tabs = 0;
        for(int i = 0; i < currentLine.length(); i++){
            if(currentLine.charAt(i) != '\t')
                break;
            tabs++;
        }

        /* If there's a semicolon at the end, insert an extra tab */
        if(currentLine.trim().endsWith(":"))
            tabs++;

        return tabs;
    }

    /**
     * Calculate the size of this console.
     *
     * @return Dimension object representing the size of this console 
     */
    public Dimension getPreferredSize(){
        /* Width is equal to input width */
        int width = console.getPreferredSize().width;
        /* Height is equal to sum of the heights plus a little buffer */
        int height = console.getPreferredSize().height + 5 + areaScrollPane.getPreferredSize().height;

        Dimension size = new Dimension(width, height);
        return size;
    }
}
