package com.scriptrts.core;

import java.util.ArrayList;
import java.awt.Font;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.io.StringWriter;

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

        buffer = "";
        history = new ArrayList<String>();

        setBackground(new Color(0, 0, 0, 0));
        int rows = consoleHeight / charHeight, columns = screenWidth / charWidth + 1;
        originalNumRows = rows;

        Color color = new Color(0, 0, 0, 120);
        Color gray = new Color(220, 220, 220, 255);
        console = new JTextField(columns);
        console.setFont(font);
        console.setBackground(color);
        console.setForeground(gray);
        console.setCaretColor(Color.white);

        output = new JTextArea(rows, columns);
        output.setLineWrap(true);
        output.setFont(font);
        output.setEditable(false);
        output.setBackground(new Color(0, 0, 0, 0));
        output.setForeground(gray);

        areaScrollPane = new JScrollPane(output);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        Dimension dim = output.getPreferredSize();
        dim.height += 5;
        areaScrollPane.setPreferredSize(dim);
        areaScrollPane.setBackground(color);

        setLayout(null);
        add(areaScrollPane);
        add(console);

        Dimension oDim = areaScrollPane.getPreferredSize();
        areaScrollPane.setBounds(0, 0, screenWidth, oDim.height);
        Dimension cDim = console.getPreferredSize();
        console.setBounds(0, oDim.height, cDim.width, cDim.height);

        console.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                runCommand();
            }
        });
        console.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent ev){
                /* History */
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

    private String terminalText = "";
    private boolean interrupted = false;
    private void stopExecution(){
        executeScript("core.interrupt()", null);
        running = false;
        interrupted = true;
    }
    private void cursorToStart(){
        console.setCaretPosition(0);
    }

    private void cursorToEnd(){
        console.setCaretPosition(console.getText().length());
    }

    private void deleteLineRemainder(){
        int loc = console.getCaretPosition();
        String substr = console.getText().substring(0, loc);
        console.setText(substr);
    }

    public void addKeyListener(KeyListener list){
        super.addKeyListener(list);
        console.addKeyListener(list);
        output.addKeyListener(list);
    }

    public boolean requestFocusInWindow(){
        return console.requestFocusInWindow();
    }

    public boolean hasFocus(){
        return console.hasFocus() || output.hasFocus();
    }

    private int currentHistoryLocation = -1;
    private void historyUp(){
        if(currentHistoryLocation > 0){
            String prevVal = getTextFromHistory();
            while(currentHistoryLocation > 0&& prevVal.equals(getTextFromHistory())) 
                currentHistoryLocation--;

            console.setText(getTextFromHistory());
        }
    }
    private void historyDown(){
        if(currentHistoryLocation < history.size()){
            String prevVal = getTextFromHistory();
            while(currentHistoryLocation < history.size() && prevVal.equals(getTextFromHistory()))
                currentHistoryLocation++;

            console.setText(getTextFromHistory());
        }
    }
    private String getTextFromHistory(){
        return getTextFromHistory(currentHistoryLocation);
    }
    private String getTextFromHistory(int ind){
        if(ind < history.size())
            return (history.get(ind));
        else
            return ("");
    }

    public void updateSize(int consoleHeight, int screenWidth){
        int rows = consoleHeight / charHeight, columns = screenWidth / charWidth + 1;
        originalNumRows = rows;
        output.setText("");

        console.setColumns(columns);
        output.setColumns(columns);
        output.setRows(rows);

        Dimension dim = output.getPreferredSize();
        dim.height += 5;
        areaScrollPane.setPreferredSize(dim);

        Dimension oDim = areaScrollPane.getPreferredSize();
        areaScrollPane.setBounds(0, 0, screenWidth, oDim.height);
        Dimension cDim = console.getPreferredSize();
        console.setBounds(0, oDim.height, cDim.width, cDim.height);
    }

    /* Remove things from the console to clear it up */
    private int histStart = 0;
    private void clearConsole(){
        output.setRows(originalNumRows);
        output.setText("");
        terminalText = "";
    }

    private String totalCommandText = "";
    private int commandTextLines = 0;
    private void runCommand(){
        /* Don't allow running commands while others are still running */
        if(running)
            return;
        String commandText = console.getText();
        console.setText("");

        /* Find out how many tabs to put in */
        int tabsForNextLine = getNextLineIndents(commandText);

        /* Add command to console */
        if(totalCommandText.equals("")) {
            terminalText += ">>>";

            if(tabsForNextLine == 0)
                terminalText += " ";
            else
                terminalText += "\n";
        }

        terminalText +=  commandText + "\n";

        /* Store the line for later execution */
        totalCommandText += commandText + "\n";
        commandTextLines++;

        /* Add the command to history */
        if(commandText.trim().length() != 0){
            history.add(commandText);
            currentHistoryLocation = history.size() - 1;
        }
        output.setText(terminalText);

        /* If this is a finished command, execute it */
        if(tabsForNextLine == 0){
            /* Start a new thread for the python command */
            new Thread(new Runnable(){
                public void run(){
                    running = true;
                    
                    String originalText = output.getText();
                    final StringWriter outputter = new StringWriter();
                    outputter.write(originalText);
                    Thread interpreter = new Thread(new Runnable(){
                        public void run(){
                            executeScript(totalCommandText, outputter);
                            terminalText = outputter.toString();
                            output.setText(terminalText);
                            running = false;
                        }
                    });
                    interpreter.setPriority(Thread.MIN_PRIORITY);
                    interpreter.start();

                    while(running){
                        terminalText = outputter.toString();

                        output.setText(terminalText);
                        output.setCaretPosition(output.getText().length());
                        try {
                            Thread.currentThread().sleep(300);
                        } catch (Exception e) { e.printStackTrace(); }
                    }

                    totalCommandText = "";
                    terminalText = outputter.toString();
                    if(interrupted){
                        terminalText += "KeyboardInterrupt\n";
                        interrupted = false;
                    }
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

    private String executeScript(String cmd, StringWriter writer){
        if(!Script.initialized()) {
            Script.initialize();
        }

        return Script.exec(cmd, writer);
    }

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

    public Dimension getPreferredSize(){
        Dimension newSize = new Dimension(console.getPreferredSize().width, 
                console.getPreferredSize().height + 5 + areaScrollPane.getPreferredSize().height);
        return newSize;
    }
}
