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
        return exec(cmd, null);
    }

    public static String exec(String cmd, StringWriter writer){
        if(writer == null)
            writer = new StringWriter();
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
            writer.write(expr);
            writer.flush();
            return writer.toString();
        } catch (Exception e) {
            writer.write(e.getMessage().trim() + "\n");
            writer.flush();
            return writer.toString();
        } 
    }
}
