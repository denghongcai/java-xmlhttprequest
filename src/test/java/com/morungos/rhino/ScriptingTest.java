package com.morungos.rhino;

import static junit.framework.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.lang.reflect.InvocationTargetException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;

public class ScriptingTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private void testScript(String script) throws IllegalAccessException, InstantiationException, InvocationTargetException {
		
		try {
			Context cx = Context.enter();
			Scriptable scope = cx.initStandardObjects();
			ScriptableObject.defineClass(scope, XMLHttpRequest.class);
			
			cx.evaluateString(scope, script, "<cmd>", 1, null);
		} finally {
			Context.exit();
		}
	}

	@Test
	public void testInstantiation() throws IllegalAccessException, InstantiationException, InvocationTargetException {
		StringBuilder script = new StringBuilder();
		script.append("var request = new XMLHttpRequest();\n");
		
		testScript(script.toString());
	}

	@Test
	public void testInstantiationReadyState() throws IllegalAccessException, InstantiationException, InvocationTargetException {
		StringBuilder script = new StringBuilder();
		script.append("var request = new XMLHttpRequest();\n");
		script.append("org.junit.Assert.assertEquals(0, request.readyState, 0.0);\n");
		
		testScript(script.toString());
	}

	@Test
	public void testInvalidCall() throws IllegalAccessException, InstantiationException, InvocationTargetException {
		StringBuilder script = new StringBuilder();
		script.append("var request = new XMLHttpRequest();\n");
		script.append("request.open('GET', 'http://google.com', 'Ninety-nine');\n");
		script.append("request.send();\n");
		
		thrown.expect(WrappedException.class);
		thrown.expectMessage(containsString("Invalid value for async"));

		testScript(script.toString());
	}

	@Test
	public void testBasicCall() throws IllegalAccessException, InstantiationException, InvocationTargetException {
		StringBuilder script = new StringBuilder();
		script.append("var request = new XMLHttpRequest();\n");
		script.append("request.open('GET', 'http://google.com');\n");
		script.append("request.send();\n");
		
		testScript(script.toString());
	}
}
