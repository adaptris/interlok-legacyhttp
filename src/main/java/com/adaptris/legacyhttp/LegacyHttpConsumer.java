package com.adaptris.legacyhttp;

import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.http.server.HeaderHandler;
import com.adaptris.core.http.server.ParameterHandler;
import com.adaptris.core.security.access.IdentityVerifier;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.TimeInterval;
import com.sun.net.httpserver.HttpExchange;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Consumer implementation to be used with {@link LegacyHttpConnection}.
 * 
 * <p>
 * There are some key differences between this and the standard jetty consumers :
 * </p>
 * <ul>
 * <li>No support for in-line authentication; you can still use a {@link IdentityVerifier} as part of your service chain</li>
 * <li>Currently no support for {@code object metadata} style parameter/header handling</li>
 * <li>No support for {@code Expect: 102-Processing} directives</li>
 * <li>The HTTP method is not filtered based on {@link ConsumeDestination#getFilterExpression()}, so implicitly all methods are
 * passed to the workflow.</li>
 * <li>{@link CoreConstants#JETTY_URL} will not be populated (as this does not appear to be available from {@link HttpExchange}, but
 * {@link CoreConstants#JETTY_URI}, {@link CoreConstants#HTTP_METHOD}, {@link CoreConstants#JETTY_QUERY_STRING} will.</li>
 * </ul>
 * 
 * @author ellidges
 *
 */
@XStreamAlias("legacy-http-consumer")
@AdapterComponent
@ComponentProfile(summary = "Listen for HTTP traffic on the specified URI", tag = "consumer,http",
    recommended = {LegacyHttpConnection.class})
@DisplayOrder(order = {"destination"})
public class LegacyHttpConsumer extends AdaptrisMessageConsumerImp {

  @Valid
  @NotNull
  @AutoPopulated
  private TimeInterval requestTimeout;
  @AutoPopulated
  @Valid
  @NotNull
  @AdvancedConfig
  private ParameterHandler<HttpExchange> parameterHandler;
  @AutoPopulated
  @Valid
  @NotNull
  @AdvancedConfig
  private HeaderHandler<HttpExchange> headerHandler;
  
  public LegacyHttpConsumer() {
    setRequestTimeout(new TimeInterval(30L, TimeUnit.SECONDS));
    setParameterHandler(new NoOpParameterHandler());
    setHeaderHandler(new NoOpHeaderHandler());
  }
  
  public LegacyHttpConsumer(ConsumeDestination d) {
    this();
    setDestination(d);
  }

  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  @Override
  public void prepare() throws CoreException {
    try {
      Args.notNull(getParameterHandler(), "parameter-handler");
      Args.notNull(getHeaderHandler(), "header-handler");
      Args.notNull(getRequestTimeout(), "request-timeout");
    }
    catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
    
  }

  public TimeInterval getRequestTimeout() {
    return requestTimeout;
  }

  /**
   * The maximum time that the server should wait for a workflow to complete before returning an error response. Default is 30
   * seconds.
   * 
   * @param requestTimeout the timeout, default is 30 seconds.
   */
  public void setRequestTimeout(TimeInterval requestTimeout) {
    this.requestTimeout = Args.notNull(requestTimeout, "requestTimeout");
  }

  public ParameterHandler<HttpExchange> getParameterHandler() {
    return parameterHandler;
  }

  /**
   * Set the handler for parameters.
   * 
   * @param s the handler
   */
  public void setParameterHandler(ParameterHandler<HttpExchange> s) {
    this.parameterHandler = Args.notNull(s, "parameterHandler");
  }

  public HeaderHandler<HttpExchange> getHeaderHandler() {
    return headerHandler;
  }

  /**
   * Set the handler for headers.
   * 
   * @param s the handler
   */
  public void setHeaderHandler(HeaderHandler<HttpExchange> s) {
    this.headerHandler = Args.notNull(s, "headerHandler");
  }
}
