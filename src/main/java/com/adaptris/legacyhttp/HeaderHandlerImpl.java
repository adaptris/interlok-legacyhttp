package com.adaptris.legacyhttp;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.server.HeaderHandler;
import com.sun.net.httpserver.HttpExchange;

/**
 * Abstract {@link HeaderHandler} implementation that provides a prefix.
 * 
 *
 */
public abstract class HeaderHandlerImpl implements HeaderHandler<HttpExchange> {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  @InputFieldDefault(value = "")
  private String headerPrefix;

  @InputFieldDefault(value = ";")
  @InputFieldHint(style = "BLANKABLE")
  private String valueSeparator;

  public HeaderHandlerImpl() {

  }

  public String getHeaderPrefix() {
    return headerPrefix;
  }

  @Override
  public void handleHeaders(AdaptrisMessage msg, HttpExchange exchange) {
    handleHeaders(msg, exchange, headerPrefix());
  }

  protected abstract void handleHeaders(AdaptrisMessage msg, HttpExchange exchange, String itemPrefix);

  public void setHeaderPrefix(String headerPrefix) {
    this.headerPrefix = headerPrefix;
  }

  /**
   * Return the header prefix with null protection.
   * 
   * @return the prefix
   */
  protected String headerPrefix() {
    return defaultIfEmpty(getHeaderPrefix(), "");
  }

  public String getValueSeparator() {
    return valueSeparator;
  }

  /**
   * Set the separator between values where multiple headers are present.
   * 
   * @param headerSeparator the separator, default is {@code ";"} if not specified
   */
  public void setValueSeparator(String headerSeparator) {
    this.valueSeparator = headerSeparator;
  }

  protected String valueSeparator() {
    return getValueSeparator() == null ? ";" : getValueSeparator();
  }
}
