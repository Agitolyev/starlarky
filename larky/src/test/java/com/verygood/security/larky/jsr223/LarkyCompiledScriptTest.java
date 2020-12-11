package com.verygood.security.larky.jsr223;

import static org.junit.Assert.assertEquals;

import com.verygood.security.larky.parser.ParsedStarFile;

import net.starlark.java.annot.Param;
import net.starlark.java.annot.StarlarkMethod;
import net.starlark.java.eval.StarlarkValue;
import org.junit.Test;

import java.io.StringWriter;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

public class LarkyCompiledScriptTest {

  @Test
  public void testGetEngine() throws ScriptException {
    LarkyScriptEngineFactory factory = new LarkyScriptEngineFactory();
    LarkyScriptEngine engine = (LarkyScriptEngine) factory.getScriptEngine();
    String script = "print(\"Hello World!!!\")";
    LarkyCompiledScript instance = (LarkyCompiledScript) engine.compile(script);

    Object expResult = "Larky ScriptEngine";
    Object result = instance.getEngine().getFactory().getEngineName();
    assertEquals(expResult, result);
  }

  @Test
  public void testEval() throws ScriptException {
    LarkyScriptEngineFactory factory = new LarkyScriptEngineFactory();
    LarkyScriptEngine engine = (LarkyScriptEngine) factory.getScriptEngine();
    String script = String.join("\n",
        "" +
        "def main():",
        "    return 'Hello World'",
        "",
        "output = main()"
    );

    LarkyCompiledScript instance = (LarkyCompiledScript) engine.compile(script);
    String expResult = "Hello World";
    ParsedStarFile result = (ParsedStarFile) instance.eval();
    assertEquals(expResult, result.getGlobalEnvironmentVariable("output", String.class));
  }

  @Test
  public void testEval_context() throws Exception {
    LarkyScriptEngineFactory factory = new LarkyScriptEngineFactory();
    LarkyScriptEngine engine = (LarkyScriptEngine) factory.getScriptEngine();
    ScriptContext context = new SimpleScriptContext();
    StringWriter writer = new StringWriter();
    StringWriter errorWriter = new StringWriter();
    context.setWriter(writer);
    context.setErrorWriter(errorWriter);

    context.setAttribute("message", "Hello World!!!!!", ScriptContext.ENGINE_SCOPE);
    engine.setContext(context);
    String script = "print(message)";
    LarkyCompiledScript instance = (LarkyCompiledScript) engine.compile(script);
    Object expResult = "Hello World!!!!!";
    instance.eval(context);
    Object result = writer.toString().trim();
    assertEquals(expResult, result);
    writer.close();
    errorWriter.close();
  }

  @Test
  public void testEval_bindings() throws Exception {
    LarkyScriptEngineFactory factory = new LarkyScriptEngineFactory();
    LarkyScriptEngine engine = (LarkyScriptEngine) factory.getScriptEngine();
    Bindings bindings = new SimpleBindings();
    bindings.put("message", "Helloooo Woooorld!");
    bindings.put("concatenator", new Xyz());
    engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
    String script = String.join("\n",
        "" +
        "def main():",
        "    return concatenator.concat('a', 'b')",
        "",
        "output = main()"
    );
    LarkyCompiledScript instance = (LarkyCompiledScript) engine.compile(script);
    Object expResult = "I heard, Helloooo Woooorld!";
    ParsedStarFile result = (ParsedStarFile) instance.eval(bindings);
    assertEquals(expResult, result.getGlobalEnvironmentVariable("output", String.class));
  }

  class Xyz implements StarlarkValue {

    @StarlarkMethod(
        name="concat2",
        parameters = {
            @Param(name = "input1"),
            @Param(name = "input2")
        }
    )
    public String concat(String a, String b) {
      return String.format("%s.%s", a, b);
    }

    @StarlarkMethod(
        name="concat3",
        parameters = {
            @Param(name = "input1"),
            @Param(name = "input2")
        }
    )
    public String concat(String a, String b, String c) {
      return String.format("%s.%s.%s", a, b, c);
    }
  }

}