package com.scriptrts.script;

import java.io.StringWriter;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptContext;
import javax.script.ScriptException;

public class Script {
    private static ScriptEngine engine = null;

    public static boolean initialized(){
        return engine != null;
    }

    public static void initialize(){
        engine = new ScriptEngineManager().getEngineByName("python");
    }

    public static String exec(String cmd){
        StringWriter writer = new StringWriter();
        StringWriter errWriter = new StringWriter();
        try {
            /* Get the output as a string */
            ScriptContext context = engine.getContext();
            context.setWriter(writer);
            context.setErrorWriter(errWriter);

            /* Evaluate the command */
            Object result = engine.eval(cmd, context);

            /* If this was an expression, append the expression value to the end of the string */
            String expr = "";
            if(result != null)
                expr = result.toString() + "\n";

            /* Return the output string */
            writer.flush();
            return writer.toString() + expr;
        } catch (Exception e) {
            return e.getMessage().trim() + "\n";
        } 
    }
}
