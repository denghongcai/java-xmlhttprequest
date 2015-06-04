package com.morungos.xmlhttprequest;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;

public class ScriptingTestBase {

	protected ScriptEngine getEngine() {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("nashorn");
		return engine;
	}
	
	protected ScriptContext getContext(ScriptEngine engine) {
		ScriptContext context = new SimpleScriptContext();
		Bindings bindings = engine.createBindings();
		context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		return context;
	}

}
