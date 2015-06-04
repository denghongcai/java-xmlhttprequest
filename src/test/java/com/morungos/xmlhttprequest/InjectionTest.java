package com.morungos.xmlhttprequest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;

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
		applicationProperties.put("interrupted", new Boolean(false));
		
		final ScriptEngine engine = getEngine();
		
		engine.put("applicationProperties", applicationProperties);
		
		
		InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("polyfill.nashorn.js");
		
		engine.eval(new InputStreamReader(resourceStream));

		String wrapped = "function run() { eventLoop(); }";
		engine.eval(wrapped);

		Invocable inv = (Invocable) engine;
				
		Runnable r = inv.getInterface(Runnable.class);
		
		Thread th = new Thread(r);
		th.start();
		
		// Now. At this stage we can theoretically inject new function calls. 
		// Theoretically.... 
		// We can do this by pushing them directly in as a new timer task. 

		Phaser phaser = (Phaser) applicationProperties.get("phaser");
		Timer timer = (Timer) applicationProperties.get("timer");
		
		Thread.sleep(200);
		phaser.register();
		timer.schedule(new TimerTask() {
			public void run() {
				try {
					engine.eval("applicationProperties.interrupted = true;");
					engine.eval("shutdown();");
				} catch (ScriptException e) {
					e.printStackTrace();
				}
			}
		}, 0);
		
		th.join();
		
		Assert.assertTrue((Boolean)applicationProperties.get("interrupted"));
	}

}
