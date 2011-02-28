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
        try {
            /* Get the output as a string */
            ScriptContext context = engine.getContext();

            StringWriter writer = new StringWriter();
            StringWriter errWriter = new StringWriter();
            context.setWriter(writer);
            context.setErrorWriter(errWriter);

            /* Evaluate the command */
            engine.eval(cmd, context);

            /* Return the output string */
            writer.flush();
            errWriter.flush();
            String ret = writer.toString();
            return ret;
        } catch (Exception e) {
            return "<Error Occurred>";
        } 
    }
}
