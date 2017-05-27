/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.jar.Pack200;

import com.dianping.pigeon.log.Logger;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.monitor.trace.InvokerMonitorData;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePhase;
import com.dianping.pigeon.remoting.common.domain.InvocationContext.TimePoint;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.ApplicationException;
import com.dianping.pigeon.remoting.common.exception.BadResponseException;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.monitor.SizeMonitor;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.InvocationUtils;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.exception.RequestTimeoutException;
import com.dianping.pigeon.remoting.invoker.process.DegradationManager;
import com.dianping.pigeon.remoting.invoker.process.ExceptionManager;
import com.dianping.pigeon.remoting.invoker.process.filter.DegradationFilter;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;

public class ServiceFutureImpl extends CallbackFuture implements Future {

	private static final Logger logger = LoggerLoader.getLogger(ServiceFutureImpl.class);

	private long timeout = Long.MAX_VALUE;

	protected Thread callerThread;

	protected InvokerContext invocationContext;

	public ServiceFutureImpl(InvokerContext invocationContext, long timeout) {
		super();
		this.invocationContext = invocationContext;
		this.timeout = timeout;
		callerThread = Thread.currentThread();
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		return get(this.timeout);
	}

	public Object get(long timeoutMillis) throws InterruptedException, ExecutionException {
		InvocationResponse response = null;
		String addr = null;
		if (client != null) {
			addr = client.getAddress();
		}
		String callInterface = InvocationUtils.getRemoteCallFullName(invocationContext.getInvokerConfig().getUrl(),
				invocationContext.getMethodName(), invocationContext.getParameterTypes());
		transaction = monitor.createTransaction("PigeonFuture", callInterface, invocationContext);
		if (transaction != null) {
			transaction.setStatusOk();
			transaction.logEvent("PigeonCall.callType", invocationContext.getInvokerConfig().getCallType(), "");
			transaction.logEvent("PigeonCall.serialize", ""
					+ (request == null ? invocationContext.getInvokerConfig().getSerialize() : request.getSerialize()),
					"");
			transaction.logEvent("PigeonCall.timeout", timeoutMillis + "",
					invocationContext.getInvokerConfig().getTimeout() + "");
			Client client = invocationContext.getClient();
			if (client != null) {
				String targetApp = RegistryManager.getInstance().getReferencedAppFromCache(client.getAddress());
				transaction.logEvent("PigeonCall.app", targetApp, "");
				transaction.logEvent("PigeonCall.server", client.getAddress(), "");
			}
			invocationContext.getTimeline().add(new TimePoint(TimePhase.F, System.currentTimeMillis()));
		}
		boolean isSuccess = false;
		try {
			try {
				response = super.waitResponse(timeoutMillis);
				if (transaction != null && response != null) {
					String size = SizeMonitor.getInstance().getLogSize(response.getSize());
					if (size != null) {
						transaction.logEvent("PigeonCall.responseSize", size, "" + response.getSize());
					}
					invocationContext.getTimeline().add(new TimePoint(TimePhase.R, response.getCreateMillisTime()));
					invocationContext.getTimeline().add(new TimePoint(TimePhase.F, System.currentTimeMillis()));
				}
			} catch (RuntimeException e) {
				if (DegradationManager.INSTANCE.needFailureDegrade(invocationContext)) { // failure degrade condition
					InvocationResponse degradedResponse = null;
					try {
						invocationContext.getDegradeInfo().setFailureDegrade(true);
						invocationContext.getDegradeInfo().setCause(e);
						degradedResponse = DegradationFilter.degradeCall(invocationContext);
					} catch (Throwable t) {
						// won't happen
						logger.warn("failure degrade in future call type error: " + t.toString());
					}
					if (degradedResponse != null) {// 返回失败降级结果
						Object responseReturn = degradedResponse.getReturn();
						if  (responseReturn instanceof RuntimeException) {
							throw (RuntimeException) responseReturn;
						} else if (responseReturn instanceof Throwable) {
							throw new ApplicationException((Throwable) responseReturn);
						} else {
							return responseReturn;
						}
					}
				}

				// not failure degrade
				DegradationManager.INSTANCE.addFailedRequest(invocationContext, e);
				ExceptionManager.INSTANCE.logRpcException(addr, invocationContext.getInvokerConfig().getUrl(),
						invocationContext.getMethodName(), "error with future call", e, request, response, transaction);
				throw e;
			}

			setResponseContext(response);

			if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
				isSuccess = true;
				DegradationManager.INSTANCE.addNormalRequest(invocationContext);
				return response.getReturn();
			} else if (response.getMessageType() == Constants.MESSAGE_TYPE_EXCEPTION) {
				if (DegradationManager.INSTANCE.needFailureDegrade(invocationContext)) { // failure degrade condition
					InvocationResponse degradedResponse = null;
					try {
						invocationContext.getDegradeInfo().setFailureDegrade(true);
						invocationContext.getDegradeInfo().setCause(InvokerUtils.toRpcException(response));
						degradedResponse = DegradationFilter.degradeCall(invocationContext);
					} catch (Throwable t) {
						// won't happen
						logger.warn("failure degrade in future call type error: " + t.toString());
					}
					if (degradedResponse != null) {// 返回失败降级结果
						Object responseReturn = degradedResponse.getReturn();
						if  (responseReturn instanceof RuntimeException) {
							throw (RuntimeException) responseReturn;
						} else if (responseReturn instanceof Throwable) {
							throw new ApplicationException((Throwable) responseReturn);
						} else {
							return responseReturn;
						}
					}
				}

				// not failure degrade
				RpcException e = ExceptionManager.INSTANCE.logRemoteCallException(addr,
						invocationContext.getInvokerConfig().getUrl(), invocationContext.getMethodName(),
						"remote call error with future call", request, response, transaction);
				if (e != null) {
					DegradationManager.INSTANCE.addFailedRequest(invocationContext, e);
					throw e;
				}
			} else if (response.getMessageType() == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {
				isSuccess = true;
				Throwable e = ExceptionManager.INSTANCE
						.logRemoteServiceException("remote service biz error with future call", request, response);

				if (DegradationManager.INSTANCE.needFailureDegrade(invocationContext)
						&& DegradationManager.INSTANCE.isCustomizedDegradeException(e)) { // failure degrade condition
					InvocationResponse degradedResponse = null;
					try {
						invocationContext.getDegradeInfo().setFailureDegrade(true);
						invocationContext.getDegradeInfo().setCause(e);
						degradedResponse = DegradationFilter.degradeCall(invocationContext);
					} catch (Throwable t) {
						// won't happen
						logger.warn("failure degrade in future call type error: " + t.toString());
					}

					if (degradedResponse != null) {// 返回失败降级结果
						Object responseReturn = degradedResponse.getReturn();
						if  (responseReturn instanceof RuntimeException) {
							throw (RuntimeException) responseReturn;
						} else if (responseReturn instanceof Throwable) {
							throw new ApplicationException((Throwable) responseReturn);
						} else {
							return responseReturn;
						}
					} else { // 没失败降级成功,由于是业务指定降级异常,也得计入失败统计
						DegradationManager.INSTANCE.addFailedRequest(invocationContext, e);
					}
				} else {
					DegradationManager.INSTANCE.addNormalRequest(invocationContext);
				}

				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				} else if (e != null) {
					throw new ApplicationException(e);
				}
			}

			RpcException e = new BadResponseException(response.toString());
			throw e;
		} finally {
			if (transaction != null) {
				DegradationManager.INSTANCE.monitorDegrade(invocationContext, transaction);

				invocationContext.getTimeline().add(new TimePoint(TimePhase.E, System.currentTimeMillis()));
				try {
					transaction.complete();
				} catch (RuntimeException e) {
					monitor.logMonitorError(e);
				}
			}

			InvokerMonitorData monitorData = (InvokerMonitorData) invocationContext.getMonitorData();
			if (monitorData != null) {
				monitorData.setIsSuccess(isSuccess);
				monitorData.complete();
			}
		}
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws java.lang.InterruptedException,
			java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
		long timeoutMs = unit.toMillis(timeout);
		try {
			return get(timeoutMs);
		} catch (RequestTimeoutException e) {
			throw new TimeoutException(timeoutMs + "ms timeout:" + e.getMessage());
		} catch (InterruptedException e) {
			throw e;
		}
	}

	protected void processContext() {
		Thread currentThread = Thread.currentThread();
		if (currentThread == callerThread) {
			super.processContext();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (transaction != null) {
			try {
				transaction.complete();
			} catch (RuntimeException e) {
			}
		}
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return cancel();
	}
}
