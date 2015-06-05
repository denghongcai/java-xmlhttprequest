package com.morungos.xmlhttprequest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Phaser;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InjectionTest extends ScriptingTestBase {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private Map<String, Object> applicationProperties = new HashMap<String, Object>();
	
	@Before
	public void setUp() {
		applicationProperties.clear();		
	}
	
	private Thread initializeEngineThread(ScriptEngine engine) throws ScriptException {
		engine.put("applicationProperties", applicationProperties);

		InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("polyfill.nashorn.js");
		
		engine.eval(new InputStreamReader(resourceStream));

		String wrapped = "function run() { eventLoop(); }";
		engine.eval(wrapped);

		Invocable inv = (Invocable) engine;
		Runnable r = inv.getInterface(Runnable.class);
		Thread th = new Thread(r);
		
		return th;
	}

	@Test
	public void testInjectOneEvent() throws ScriptException, InterruptedException {
		
		applicationProperties.put("interrupted", new Boolean(false));
		final ScriptEngine engine = getEngine();
		Thread th = initializeEngineThread(engine);
		th.start();
		
		Timer timer = (Timer) applicationProperties.get("timer");
		
		Thread.sleep(200);
		timer.schedule(new TimerTask() {
			public void run() {
				try {
					engine.eval("setTimeout(function() { applicationProperties.interrupted = true; shutdown(); }, 0);");
				} catch (ScriptException e) {
					e.printStackTrace();
				}
			}
		}, 0);
		
		th.join();
		
		Assert.assertTrue((Boolean)applicationProperties.get("interrupted"));
	}

	@Test
	public void testInjectMultipleEvents() throws ScriptException, InterruptedException {
		applicationProperties.put("events", new ArrayList<Object>());
		final ScriptEngine engine = getEngine();
		Thread th = initializeEngineThread(engine);
		th.start();

		Timer timer = (Timer) applicationProperties.get("timer");

		String wrapped = "function trigger(event) { applicationProperties.events.add(event.event); }";
		engine.eval(wrapped);

		for(int i = 0; i < 3; i++) {
			Thread.sleep(200);
			
			final int j = i;
			
			timer.schedule(new TimerTask() {
				public void run() {
					try {
						engine.eval("setTimeout(function() { trigger({event: " + j + "}); if (" + j + " == 2) { shutdown(); }; }, 0);");
					} catch (ScriptException e) {
						e.printStackTrace();
					}
				}
			}, 0);
		}

		th.join();
		
		@SuppressWarnings("unchecked")
		List<Object> events = (List<Object>) applicationProperties.get("events");
		
		Assert.assertEquals(3, events.size());
		for(int i = 0; i < 3; i++) {
			Integer event = (Integer) events.get(i);
			Assert.assertEquals(new Integer(i), event);
		}
	}
}
