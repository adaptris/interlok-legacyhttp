package com.adaptris.legacyhttp;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.server.HeaderHandler;
import com.sun.net.httpserver.HttpExchange;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link HeaderHandler} implementation that ignores HTTP headers.
 * 
 * @config legacy-http-ignore-headers
 * 
 */
@XStreamAlias("legacy-http-ignore-headers")
public class NoOpHeaderHandler implements HeaderHandler<HttpExchange> {

  @Override
  public void handleHeaders(AdaptrisMessage message, HttpExchange request) {
  }

}
