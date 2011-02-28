package com.scriptrts.core;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.scriptrts.script.*;

public class Console extends JPanel {
    private String buffer;
    private ArrayList<String> history;
    private ArrayList<Integer> commandLengths;
    private ArrayList<String> outputs;

    private static Font font = new Font("Monospaced", Font.PLAIN, 14);
    private static int charWidth = -1;
    private static int charHeight = -1;
    private JTextField console;
    private int originalNumRows = -1;
    private JScrollPane areaScrollPane;
    private JTextArea output;

    public static boolean calibrated(){
        return !(charWidth < 0 || charHeight < 0);
    }

    public static void calibrateFont(Graphics graphics){
        if(charWidth < 0 || charHeight < 0) {
            FontMetrics metrics = graphics.getFontMetrics(font);
            charWidth = metrics.charWidth('A');
            charHeight = metrics.getHeight();
        }
    }

    public Console(int consoleHeight, int screenWidth){
        super(true);

        buffer = "";
        history = new ArrayList<String>();
        commandLengths = new ArrayList<Integer>();
        outputs = new ArrayList<String>();

        setBackground(new Color(0, 0, 0, 0));
        int rows = consoleHeight / charHeight, columns = screenWidth / charWidth + 1;
        originalNumRows = rows;

        Color color = new Color(0, 0, 0, 120);
        Color gray = new Color(220, 220, 220, 255);
        console = new JTextField(columns);
        console.setFont(font);
        console.setBackground(color);
        console.setForeground(gray);

        output = new JTextArea(rows, columns);
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

                /* Deleting remainder of line with Ctrl-k */
                else if(ev.getKeyCode() == KeyEvent.VK_K){
                    if(ev.isControlDown())
                        deleteLineRemainder();
                }
            }
        });

        updateOutput();
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

        updateOutput();
    }

    /* Remove things from the console to clear it up */
    private int histStart = 0;
    private int outputsStart = 0;
    private void clearConsole(){
        histStart = history.size();
        outputsStart = outputs.size();
        updateOutput();
        output.setRows(originalNumRows);
    }

    private String totalCommandText = "";
    private int commandTextLines = 0;
    private void runCommand(){
        String commandText = console.getText();
        console.setText("");

        /* Store the line for later execution */
        totalCommandText += commandText + "\n";
        commandTextLines++;

        /* Add the command to history */
        history.add(commandText);
        currentHistoryLocation = history.size() - 1;

        /* Find out how many tabs to put in */
        int tabsForNextLine = getNextLineIndents(commandText);

        /* If this is a finished command, execute it */
        if(tabsForNextLine == 0){
            outputs.add(executeScript(totalCommandText));

            totalCommandText = "";
            commandLengths.add(commandTextLines);
            commandTextLines = 0;
        } 

        /* Add tabs to the next line */
        String tabs = "";
        for(int i = 0; i < tabsForNextLine; i++)
            tabs += "\t";
        console.setText(tabs);

        updateOutput();

        /* Set console history */
        currentHistoryLocation = history.size();
    }

    private String executeScript(String cmd){
        if(!Script.initialized()) {
            Script.initialize();
        }

        return Script.exec(cmd);
    }

    private void updateOutput(){
        if(history.size() == 0)
            output.setText(">>> ");
        else {
            String outputText = "";
            int histIndex = histStart;
            for(int i = outputsStart; i < commandLengths.size(); i++){
                int currentCommandLen = commandLengths.get(i);

                if(currentCommandLen == 1)
                    outputText += ">>> ";
                else
                    outputText += ">>>\n";

                outputText += history.get(histIndex) + "\n";
                histIndex++;
                for(int j = 1; j < currentCommandLen; j++){
                    outputText += history.get(histIndex) + "\n";
                    histIndex++;
                }

                if(outputText.length() != 0 && outputText.charAt(outputText.length() - 1) != '\n')
                    outputText += "\n";
                outputText += outputs.get(i);
            }

            /* Add remaining items in history which haven't been executed */
            if(histIndex < history.size())
                outputText += ">>>\n";
            for(int i = histIndex; i < history.size(); i++){
                outputText += history.get(histIndex) + "\n";
                histIndex++;
            }

            output.setText(outputText);
        }

        int lines = output.getText().split("\n").length;
        if(lines > output.getRows()) {
            output.setRows(lines);
        }
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
