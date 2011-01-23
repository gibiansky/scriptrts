import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Engine {
    public static void main(String[] args) throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");
        engine.eval("import sys");
        engine.eval("print sys");
        engine.put("a", 42);
        engine.eval("print a");
        engine.eval("x = 2 + 2");
        Object x = engine.get("x");
        System.out.println("x: " + x);
    }
}
