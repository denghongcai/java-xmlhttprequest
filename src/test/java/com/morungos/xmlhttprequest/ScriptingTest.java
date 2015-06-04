package com.morungos.xmlhttprequest;

import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ScriptingTest extends ScriptingTestBase {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private Bindings testScript(String script) throws ScriptException {
		
		ScriptEngine engine = getEngine();
		ScriptContext context = getContext(engine);
		InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("polyfill.nashorn.js");
		engine.eval(new InputStreamReader(resourceStream), context);
		
		String wrapped = "main(function() { " + script + " });";
		engine.eval(new StringReader(wrapped), context);
		
		return context.getBindings(ScriptContext.ENGINE_SCOPE);
	}

	/**
	 * Tests a timeout function.
	 * @throws ScriptException
	 */
	@Test
	public void testEventLoopWrapping() throws ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("var testDelay = function() { };\n");
		script.append("setTimeout(testDelay, 100);\n");
		
		testScript(script.toString());
	}

	/**
	 * Tests a timeout function which throws an error.
	 * @throws ScriptException
	 */
	@Test
	public void testEventLoopErrorWrapping() throws ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("var testDelay = function() { throw new Error('foo'); };\n");
		script.append("setTimeout(testDelay, 100);\n");

		thrown.expect(ScriptException.class);
		thrown.expectMessage(containsString("Error: foo"));

		testScript(script.toString());
	}

	/**
	 * Tests instantiation of XMLHttpRequest.
	 * @throws ScriptException
	 */
	@Test
	public void testInstantiationReadyState() throws ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("var request = new XMLHttpRequest();\n");
		
		script.append("org.junit.Assert.assertEquals(0, request.readyState, 0.0);\n");
		
		testScript(script.toString());
	}


	/**
	 * Tests a basic remote call through XMLHttpRequest.
	 * @throws ScriptException
	 */
	@Test
	public void testBasicCall() throws ScriptException {
		StringBuilder script = new StringBuilder();
		script.append("var request = new XMLHttpRequest();\n");
		script.append("this.success = false;\n");
		script.append("request.open('GET', 'http://httpbin.org/ip');\n");
		script.append("request.responseType = 'json';\n");
		script.append("request.onreadystatechange = function() { if (request.readyState == 4) { success = typeof request.response.origin === 'string'; } };\n");
		script.append("request.send();\n");
		
		Boolean success = (Boolean) testScript(script.toString()).get("success");
		Assert.assertTrue(success);
	}

}
