package com.adaptris.legacyhttp;

import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Consumer implementation to be used with {@link LegacyHttpConnection}.
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
  
  public LegacyHttpConsumer() {
    setRequestTimeout(new TimeInterval(30L, TimeUnit.SECONDS));
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
    this.requestTimeout = requestTimeout;
  }
}
