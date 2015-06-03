package com.morungos.xmlhttprequest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InjectionTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private Map<String, Object> applicationProperties = new HashMap<String, Object>();

	private Bindings testScript(String script) throws ScriptException {
		
		applicationProperties.clear();
		
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("nashorn");
		
		ScriptContext context = new SimpleScriptContext();
		Bindings bindings = engine.createBindings();
		bindings.put("applicationProperties", applicationProperties);
		context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		
		InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("polyfill.nashorn.js");
		
		engine.eval(new InputStreamReader(resourceStream), context);
		
		String wrapped = "main(function() { " + script + " });";
		
		engine.eval(new StringReader(wrapped), context);
		
		return bindings;
	}

	@Test
	public void testInjectEvent() throws ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("var testDelay = function() { };\n");
		script.append("setInterval(testDelay, 1000);\n");
		
		testScript(script.toString());
	}

}
