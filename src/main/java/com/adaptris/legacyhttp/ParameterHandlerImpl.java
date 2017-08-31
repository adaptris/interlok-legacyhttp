package com.adaptris.legacyhttp;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.server.ParameterHandler;
import com.sun.net.httpserver.HttpExchange;

/**
 * Abstract {@link ParameterHandler} implementation that provides a prefix.
 * 
 * @author lchan
 *
 */
public abstract class ParameterHandlerImpl implements ParameterHandler<HttpExchange> {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  @InputFieldDefault(value = "")
  private String parameterPrefix;

  public ParameterHandlerImpl() {

  }

  public String getParameterPrefix() {
    return parameterPrefix;
  }

  public void setParameterPrefix(String headerPrefix) {
    this.parameterPrefix = headerPrefix;
  }

  /**
   * Return the parameter prefix with null protection.
   * 
   * @return the prefix
   */
  protected String parameterPrefix() {
    return defaultIfEmpty(getParameterPrefix(), "");
  }

  @Override
  public void handleParameters(AdaptrisMessage message, HttpExchange request) {
    handleParameters(message, request, parameterPrefix());
  }

  protected abstract void handleParameters(AdaptrisMessage msg, HttpExchange exchange, String itemPrefix);

}
