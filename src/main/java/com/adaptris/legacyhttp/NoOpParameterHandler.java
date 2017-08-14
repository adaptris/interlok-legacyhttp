package com.adaptris.legacyhttp;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.server.ParameterHandler;
import com.sun.net.httpserver.HttpExchange;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ParameterHandler} implementation that ignores HTTP parameters.
 * 
 * @config jetty-http-ignore-parameters
 * 
 */
@XStreamAlias("legacy-http-ignore-parameters")
public class NoOpParameterHandler implements ParameterHandler<HttpExchange> {

  @Override
  public void handleParameters(AdaptrisMessage message, HttpExchange request) {
  }

}
