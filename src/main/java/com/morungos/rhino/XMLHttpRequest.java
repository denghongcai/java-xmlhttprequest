package com.morungos.rhino;

import java.io.IOException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrappedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLHttpRequest extends ScriptableObject {
	
	/**
	 * When constructed, the XMLHttpRequest object must be in the UNSENT state.
	 */
	public static final int UNSENT = 0;

	/**
	 * The OPENED state is the state of the object when the open() method has
	 * been successfully invoked. During this state request headers can be set
	 * using setRequestHeader() and the request can be made using send().
	 */
	public static final int OPENED = 1;

	/**
	 * The HEADERS_RECEIVED state is the state of the object when all response
	 * headers have been received.
	 */
	public static final int HEADERS_RECEIVED = 2;

	/**
	 * The LOADING state is the state of the object when the response entity
	 * body is being received.
	 */
	public static final int LOADING = 3;

	/**
	 * The DONE state is the state of the object when either the data transfer
	 * has been completed or something went wrong during the transfer (infinite
	 * redirects for instance).
	 */
	public static final int DONE = 4;

	/**
	 * A logger for debugging.
	 */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private int readyState = UNSENT;

	@Override
	public String getClassName() {
		return "XMLHttpRequest";
	}

	public void jsConstructor() {
		logger.info("Initialising XMLHttpRequest");
	}
	
	/**
	 * Get's the current ready-state.
	 * 
	 * @return the ready-state constant
	 * @see http://www.w3.org/TR/XMLHttpRequest/#readystate
	 */
	public int jsGet_readyState() {
		logger.info("Returning {}", this.readyState);
		return this.readyState;
	}
	
	private final class RequestCallback implements FutureCallback<Boolean> {

	    public void failed(final Exception ex) {
	        // do something
	    }

	    public void completed(final Boolean result) {
	        // do something
	    }

	    public void cancelled() {
	        // do something
	    }
	}

//	private Invocable onreadystatechange = null;
	
	private RequestBuilder builder;
	private CredentialsProvider credentialsProvider;
	private boolean async;

//	public void setOnreadystatechange(Invocable callback) {
//		onreadystatechange = callback;
//	}
//	
//	public Invocable getOnreadystatechange() {
//		return onreadystatechange;
//	}
//	
	public void jsFunction_open(String method, String url, Object async, String user, String password) {
		do_open(method, url, async, user, password);
	}
//	
//	private void changeReadyState(int state) {
//		readyState = state;
//		if (onreadystatechange != null) {
//			try {
//				onreadystatechange.invokeFunction("onreadystatechange", state);
//			} catch (NoSuchMethodException e) {
//				e.printStackTrace();
//			} catch (ScriptException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//	
	public void do_open(String method, String url, Object async, String user, String password) {
		
		logger.info("Opening: {}, {}, {}, {}, {}", method, url, async, user, password);
				
		if (async instanceof Undefined) {
			this.async = true;
		} else if (async instanceof Boolean) {
			this.async = ((Boolean) async).booleanValue();
		} else {
			throw new WrappedException(new Exception(String.format("Invalid value for async value: {}", async)));
		}
				
		builder = RequestBuilder.create(method);
		builder.setUri(url);
		
		this.async = true;
		
		if (user != "" || password != "") {
			credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
		} else {
			credentialsProvider = new SystemDefaultCredentialsProvider();
		}
	}
	
	public void jsFunction_send() {
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		HttpClient httpclient = clientBuilder.build();
		try {
			logger.info("Initiating remote request");
			httpclient.execute(builder.build());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setRequestHeader(String name, String value) {
		builder.addHeader(name, value);
	}
}
