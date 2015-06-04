package com.morungos.xmlhttprequest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InjectionTest extends ScriptingTestBase {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private Map<String, Object> applicationProperties = new HashMap<String, Object>();

	@Test
	public void testInjectEvent() throws ScriptException, InterruptedException {
		
		applicationProperties.clear();
		
		ScriptEngine engine = getEngine();
		ScriptContext context = getContext(engine);
		
		Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("applicationProperties", applicationProperties);

		InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("polyfill.nashorn.js");
		
		engine.eval(new InputStreamReader(resourceStream), context);

		StringBuilder script = new StringBuilder();
		script.append("var testDelay = function() { };\n");
		script.append("setInterval(testDelay, 1000);\n");
		
		String wrapped = "main(function() { " + script.toString() + " });";
		
		// This time, we need to be a bit more subtle in embedding. 
		Invocable inv = (Invocable) engine;
		Runnable r = inv.getInterface(Runnable.class);
		Thread th = new Thread(r);
		th.start();
		
		// Now, during this, we need to be able to figure a way to throw events into the JS 
		// thread. This needs to access the timer and the phaser, both of which are deliberately
		// well-hidden. So we only expose them when we need to, and we do it carefully. 
		
		th.join();

	}

}
