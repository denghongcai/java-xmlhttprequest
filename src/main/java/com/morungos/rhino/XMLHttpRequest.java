package com.morungos.rhino;

import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLHttpRequest {
	
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

	public String getClassName() {
		return "XMLHttpRequest";
	}

	public void XMLHttpRequest() {
		logger.info("Initialising XMLHttpRequest");
	}
	
	/**
	 * Get's the current ready-state.
	 * 
	 * @return the ready-state constant
	 * @see http://www.w3.org/TR/XMLHttpRequest/#readystate
	 */
	public int getReadyState() {
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
	public void open(String method, String url) {
		do_open(method, url, true, "", "");
	}

	public void open(String method, String url, Object async) {
		do_open(method, url, async, "", "");
	}

	public void open(String method, String url, Object async, String user) {
		do_open(method, url, async, user, "");
	}

	public void open(String method, String url, Object async, String user, String password) {
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
				
		if (async instanceof Boolean) {
			this.async = ((Boolean) async).booleanValue();
		} else {
			throw new RuntimeException(String.format("Invalid value for async value: {}", async));
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
	
	public void send() {
		if (async) {
			do_sendASynchronous();
		} else {
			do_sendSynchronous();
		}
	}
		
	public void do_sendSynchronous() {
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		HttpClient httpclient = clientBuilder.build();
		try {
			logger.info("Initiating synchronous remote request");
			httpclient.execute(builder.build());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void do_sendASynchronous() {
		HttpAsyncClientBuilder clientBuilder = HttpAsyncClientBuilder.create();
		HttpAsyncClient httpclient = clientBuilder.build();
		logger.info("Initiating asynchronous remote request");
		Future<HttpResponse> future = httpclient.execute(builder.build(), null);
	}
	
	public void setRequestHeader(String name, String value) {
		builder.addHeader(name, value);
	}
}
