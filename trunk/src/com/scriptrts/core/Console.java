package com.scriptrts.core;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.scriptrts.script.*;

public class Console extends JPanel {
    private String buffer;
    private ArrayList<String> history;
    private ArrayList<String> outputs;

    private static Font font = new Font("Monospaced", Font.PLAIN, 14);
    private static int charWidth = -1;
    private static int charHeight = -1;
    private JTextField console;
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
        outputs = new ArrayList<String>();

        setBackground(new Color(0, 0, 0, 0));
        int rows = consoleHeight / charHeight, columns = screenWidth / charWidth + 1;

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
        areaScrollPane.setBounds(0, 0, oDim.width, oDim.height);
        Dimension cDim = console.getPreferredSize();
        console.setBounds(0, oDim.height, cDim.width, cDim.height);

        console.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                runCommand();
            }
        });

        updateOutput();
    }

    public void addKeyListener(KeyListener list){
        super.addKeyListener(list);
        console.addKeyListener(list);
        output.addKeyListener(list);
    }

    public boolean requestFocusInWindow(){
        return console.requestFocusInWindow();
    }

    public void updateSize(int consoleHeight, int screenWidth){
        int rows = consoleHeight / charHeight, columns = screenWidth / charWidth + 1;

        console.setColumns(columns);
        output.setColumns(columns);
        output.setRows(rows);

        Dimension dim = output.getPreferredSize();
        dim.height += 5;
        areaScrollPane.setPreferredSize(dim);

        Dimension oDim = areaScrollPane.getPreferredSize();
        areaScrollPane.setBounds(0, 0, oDim.width, oDim.height);
        Dimension cDim = console.getPreferredSize();
        console.setBounds(0, oDim.height, cDim.width, cDim.height);

        updateOutput();
    }

    private void runCommand(){
        String commandText = console.getText();
        console.setText("");

        boolean ident = true;
        for(int i = 0; i < commandText.length(); i++)
            if(!Character.isJavaIdentifierPart(commandText.charAt(i)))
                ident = false;

        history.add(commandText);
        if(ident)
            commandText = "print " + commandText;
        outputs.add(executeScript(commandText));

        updateOutput();
    }

    private String executeScript(String cmd){
        if(!Script.initialized()) {
            Script.initialize();
        }

        return Script.exec(cmd);
    }

    private void updateOutput(){
        if(outputs.size() == 0)
            output.setText(">>> ");
        else {
            String outputText = "";
            for(int i = 0; i < history.size(); i++){
                if(outputText.length() != 0 && outputText.charAt(outputText.length() - 1) != '\n')
                    outputText += "\n";
                outputText += ">>> " + history.get(i) + "\n";
                outputText += outputs.get(i);
            }

            output.setText(outputText);
        }

        int lines = output.getText().split("\n").length;
        if(lines > output.getRows()) {
            output.setRows(lines);
        }
    }

    public Dimension getPreferredSize(){
        return new Dimension(console.getPreferredSize().width, 
                console.getPreferredSize().height + 5 + output.getPreferredSize().height);
    }
}
