package com.scriptrts.script;

import java.io.StringWriter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Provides the interface to the embedded Jython interpreter to the rest of the program.
 */
public class Script {
    /**
     * Whether to disable the scripting engine
     */
    public static boolean DISABLE = false;

    /**
     * Low-level script engine interface, used to communicate with Jython 
     */
    private static ScriptEngine engine = null;

    /**
     * Whether the interface has been initialized.
     * @return whether the engine has been initialized
     */
    public static boolean initialized(){
        /* An initialized interface will have a usable script engine */
        return engine != null;
    }

    /**
     * Initialize the interpreter.
     */
    public static void initialize(){
        /* Create the script engine. Jython.jar should be in the classpath */
        if(!DISABLE)
            engine = new ScriptEngineManager().getEngineByName("python");
    }

    /**
     * Runs the provided Python command(s).
     *
     * @param cmd the python code to interpret
     * @return the resulting output, as if we were writing to a console (includes expression values, exception messages)
     */
    public static String exec(String cmd){
        return exec(cmd, null);
    }

    /**
     * Runs the provided Python command(s).
     *
     * @param cmd the python code to interpret
     * @param writer an output writer to which Jython will direct stdout and stderr, as well as any other output
     * @return the resulting output, as if we were writing to a console (includes expression values, exception messages)
     */
    public static String exec(String cmd, StringWriter writer){
        /* If initialization hasn't been done, do it now */
        if(!Script.initialized())
            Script.initialize();

        if(DISABLE){
            if(writer != null)
                writer.write("Error: Python disabled.\n");
            return "Error: Python disabled.\n";
        }

        /* If we don't need to write to a specified place, just write somewhere to later collect the String */
        if(writer == null)
            writer = new StringWriter();

        StringWriter errWriter = new StringWriter();
        try {
            /* Set stdout and stderr to our string writers */
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
        } 
        
        /* If an error occurred */
        catch (Exception e) {
            /* Print the error to the console, return the console value */
            writer.write(e.getMessage().trim() + "\n");
            writer.flush();

            return writer.toString();
        } 
    }
}
