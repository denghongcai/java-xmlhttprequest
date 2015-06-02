package com.morungos.rhino;

import static junit.framework.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ScriptingTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private void testScript(String script) throws ScriptException {
		
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("nashorn");
		
		ScriptContext context = new SimpleScriptContext();
		Bindings bindings = engine.createBindings();
		context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		
		InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("polyfill.nashorn.js");
		
		engine.eval(new InputStreamReader(resourceStream), context);
		
		String wrapped = "main(function() { " + script + " });";
		
		engine.eval(new StringReader(wrapped), context);
	}

	@Test
	public void testEventLoopWrapping() throws ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("var testDelay = function() { };\n");
		script.append("setTimeout(testDelay, 100);\n");
		
		testScript(script.toString());
	}

	@Test
	public void testEventLoopErrorWrapping() throws ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("var testDelay = function() { throw new Error('foo'); };\n");
		script.append("setTimeout(testDelay, 100);\n");

		thrown.expect(ScriptException.class);
		thrown.expectMessage(containsString("Error: foo"));

		testScript(script.toString());
	}

	@Test
	public void testInstantiationReadyState() throws ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("var request = new XMLHttpRequest();\n");
		
		script.append("org.junit.Assert.assertEquals(0, request.readyState, 0.0);\n");
		
		testScript(script.toString());
	}


	@Test
	public void testBasicCall() throws IllegalAccessException, InstantiationException, InvocationTargetException, ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("var request = new XMLHttpRequest();\n");
		script.append("var success = false;\n");
		script.append("request.open('GET', 'http://ip.jsontest.com');\n");
		script.append("request.responseType = 'json';\n");
		script.append("request.onreadystatechange = function() { if (request.readyState == 4) { success = typeof request.response.ip === 'string'; } };\n");
		script.append("request.send();\n");
		
		script.append("setTimeout(function() { org.junit.Assert.assertTrue(success); }, 1000);\n");

		testScript(script.toString());
	}
}
