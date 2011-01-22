package com.scriptrts.script;

public class SyntaxException extends Exception {
    public SyntaxException(String message, int line){
        super("Syntax Error (line " + line + ") - " + message);
    }
}
