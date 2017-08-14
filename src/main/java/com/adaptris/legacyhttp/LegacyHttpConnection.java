
package com.adaptris.legacyhttp;

import javax.xml.bind.annotation.XmlTransient;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisConnectionImp;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.CoreException;
import com.sun.net.httpserver.HttpServer;

/**
 * Base class for Sun HttpServer based connections using {@link HttpServer}.
 * <p>
 * Included to provide a mechanism for implementing HTTP services where Java 1.8 isn't available and therefore Jetty is not an
 * option (or you really don't want to use jetty).
 * </p>
 * 
 * @author ellidges
 */
public abstract class LegacyHttpConnection extends AdaptrisConnectionImp {
  
  /**
   * Default port for the server to listen on
   */
  public static final int DEFAULT_SERVER_PORT=9090;
  /**
   * Default hostname to bind to
   */
  public static final String DEFAULT_SERVER_HOST="localhost";
  /**
   * Default size of request backlog
   */
  public static final int DEFAULT_REQUEST_BACKLOG=10;
  
  /**
   * Object metadata key for the httpd monitor
   */
  public static final String LEGACY_HTTP_RESPONSE_QUEUE_KEY="legacyHttpResponseQueue";
  
  @XmlTransient
  protected transient HttpServer server;
  
  @InputFieldDefault(value = "9090")
  private Integer port;
  @InputFieldDefault(value = DEFAULT_SERVER_HOST)
  private String host;
  @InputFieldDefault(value = "10")
  private Integer requestBacklog;
  
  public LegacyHttpConnection() {
  }

  @Override
  public void addMessageConsumer(AdaptrisMessageConsumer consumer) throws CoreException {
    super.addMessageConsumer(consumer);
  }
  

  @Override
  protected void prepareConnection() throws CoreException {
  }

  @Override
  protected void startConnection() throws CoreException {
    try {
      server.start();
    } catch (Exception e) {
      throw new CoreException("Failed to start http server on [" + host() + ":" + port() + "]", e);
    }
  }

  @Override
  protected void stopConnection() {
    server.stop(5);
  }

  @Override
  protected void closeConnection() {
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  int port() {
    return getPort() != null ? getPort().intValue() : DEFAULT_SERVER_PORT;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  String host() {
    return getHost() != null ? getHost() : DEFAULT_SERVER_HOST;
  }

  public Integer getRequestBacklog() {
    return requestBacklog;
  }

  public void setRequestBacklog(Integer requestBacklog) {
    this.requestBacklog = requestBacklog;
  }

  int requestBacklog() {
    return getRequestBacklog() != null ? getRequestBacklog().intValue() : DEFAULT_REQUEST_BACKLOG;
  }
  
}
