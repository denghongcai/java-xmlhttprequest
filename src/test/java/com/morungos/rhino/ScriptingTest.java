package com.morungos.rhino;

import static junit.framework.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

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

	private void testScript(String script) throws IllegalAccessException, InstantiationException, InvocationTargetException, ScriptException {
		
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("nashorn");
		
		ScriptContext context = new SimpleScriptContext();
		context.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
		
		engine.eval("var XMLHttpRequest = Java.type('com.morungos.rhino.XMLHttpRequest')", context);
		engine.eval(new StringReader(script), context);
	}

	@Test
	public void testInstantiation() throws IllegalAccessException, InstantiationException, InvocationTargetException, ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("var request = new XMLHttpRequest();\n");
		
		testScript(script.toString());
	}

	@Test
	public void testInstantiationReadyState() throws IllegalAccessException, InstantiationException, InvocationTargetException, ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("var request = new XMLHttpRequest();\n");
		script.append("org.junit.Assert.assertEquals(0, request.readyState, 0.0);\n");
		
		testScript(script.toString());
	}

	@Test
	public void testInvalidCall() throws IllegalAccessException, InstantiationException, InvocationTargetException, ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("var request = new XMLHttpRequest();\n");
		script.append("request.open('GET', 'http://google.com', 'Ninety-nine');\n");
		script.append("request.send();\n");
		
		thrown.expect(RuntimeException.class);
		thrown.expectMessage(containsString("Invalid value for async"));

		testScript(script.toString());
	}

	@Test
	public void testBasicCall() throws IllegalAccessException, InstantiationException, InvocationTargetException, ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("var request = new XMLHttpRequest();\n");
		script.append("request.open('GET', 'http://google.com');\n");
		script.append("request.send();\n");
		
		testScript(script.toString());
	}
}
