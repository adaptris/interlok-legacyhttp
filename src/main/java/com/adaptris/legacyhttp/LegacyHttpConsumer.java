package com.adaptris.legacyhttp;

import java.util.concurrent.TimeUnit;

import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.CoreException;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("legacy-http-consumer")
public class LegacyHttpConsumer extends AdaptrisMessageConsumerImp {
  
  /**
   * The maximum time that the server should wait for a workflow to complete 
   * before returning an error response. Default is 30 seconds.
   */
  private TimeInterval requestTimeout;
  
  public LegacyHttpConsumer() {
    setRequestTimeout(new TimeInterval(30L, TimeUnit.SECONDS));
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

  public void setRequestTimeout(TimeInterval requestTimeout) {
    this.requestTimeout = requestTimeout;
  }
}
