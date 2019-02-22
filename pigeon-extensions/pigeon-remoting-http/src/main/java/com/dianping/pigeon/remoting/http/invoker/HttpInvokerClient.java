/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.http.invoker;

import java.net.ConnectException;
import java.util.List;

import com.dianping.pigeon.remoting.common.channel.Channel;
import com.dianping.pigeon.remoting.common.codec.SerializerType;
import com.dianping.pigeon.remoting.invoker.process.ResponseProcessor;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.NetworkException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.AbstractClient;
import com.dianping.pigeon.remoting.invoker.client.ClientConfig;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;

public class HttpInvokerClient extends AbstractClient {

	private ConnectInfo connectInfo;
	/**
	 * http的执行器
	 */
	private HttpInvokerExecutor httpInvokerExecutor;
	private String serviceUrlPrefix = null;
	private String defaultServiceUrl = null;
	private boolean isConnected = false;
	public static final String CONTENT_TYPE_SERIALIZED_OBJECT = "application/x-java-serialized-object";

	public HttpInvokerClient(ClientConfig clientConfig, ConnectInfo connectInfo, ResponseProcessor responseProcessor) {
		super(clientConfig, responseProcessor);

		this.connectInfo = connectInfo;
		if (logger.isInfoEnabled()) {
			logger.info("http client:" + connectInfo);
		}
		serviceUrlPrefix = "http://" + connectInfo.getHost() + ":" + connectInfo.getPort() + "/";
		defaultServiceUrl = serviceUrlPrefix + "service";
		httpInvokerExecutor = new HttpInvokerExecutor();
		HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setMaxTotalConnections(300);
		params.setDefaultMaxConnectionsPerHost(50);
		params.setConnectionTimeout(1000);
		params.setTcpNoDelay(true);
		params.setSoTimeout(3000);
		params.setStaleCheckingEnabled(true);
		connectionManager.setParams(params);
		HttpClient httpClient = new HttpClient();
		httpClient.setHttpConnectionManager(connectionManager);
		httpInvokerExecutor.setHttpClient(httpClient);
	}

	@Override
	public ConnectInfo getConnectInfo() {
		return connectInfo;
	}

	@Override
	public void doOpen() {
		InvocationRequest request = InvocationUtils.newRequest(Constants.HEART_TASK_SERVICE,
				Constants.HEART_TASK_METHOD, null, SerializerType.HESSIAN.getCode(), Constants.MESSAGE_TYPE_HEART, 5000,
				null);
		request.setSequence(0);
		request.setCreateMillisTime(System.currentTimeMillis());
		request.setCallType(Constants.CALLTYPE_REPLY);
		InvocationResponse response = null;
		try {
			response = this.write(request);
			if (response != null && response.getSequence() == 0) {
				isConnected = true;
			}
		} catch (Throwable e) {
			close();
			isConnected = false;
		}
	}

	@Override
	public InvocationResponse doWrite(InvocationRequest invocationRequest) throws NetworkException {
		return write(defaultServiceUrl, invocationRequest);
	}

	public InvocationResponse write(String url, InvocationRequest request) throws NetworkException {
		final int timeout = request.getTimeout();
		httpInvokerExecutor.setReadTimeout(timeout);
		try {
			InvocationResponse invocationResponse = httpInvokerExecutor.executeRequest(url, request);
			this.isConnected = true;
			return invocationResponse;
		} catch (ConnectException e) {
			this.isConnected = false;
			throw new NetworkException("remote call failed:" + url + ", request:" + request, e);
		} catch (Throwable e) {
			throw new NetworkException("remote call failed:" + url + ", request:" + request, e);
		}
	}

	@Override
	public String getHost() {
		return connectInfo.getHost();
	}

	@Override
	public String getAddress() {
		return connectInfo.getHost() + ":" + connectInfo.getPort();
	}

	@Override
	public int getPort() {
		return connectInfo.getPort();
	}

	@Override
	public boolean isActive() {
		return super.isActive() && isConnected;
	}

	@Override
	public void doClose() {
	}

	@Override
	public List<Channel> getChannels() {
		return null;
	}

	@Override
	public String toString() {
		return this.getAddress();
	}

	public boolean equals(Object obj) {
		if (obj instanceof HttpInvokerClient) {
			HttpInvokerClient nc = (HttpInvokerClient) obj;
			return this.getAddress().equals(nc.getAddress());
		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int hashCode() {
		return getAddress().hashCode();
	}

	@Override
	public String getProtocol() {
		return Constants.PROTOCOL_HTTP;
	}

}
