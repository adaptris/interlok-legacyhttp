
package com.adaptris.legacyhttp;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.xml.bind.annotation.XmlTransient;

import com.adaptris.core.AdaptrisConnectionImp;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.CoreException;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Base class for Sun HttpServer based connections. Included to provide a mechanism
 * for implementing HTTP services where Java 1.8 isn't available and therefore
 * Jetty is not an option.
 * @author ellidges
 */
@XStreamAlias("legacy-http-connection")
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
   * Default size of threadpool
   */
  public static final int DEFAULT_REQUEST_BACKLOG=10;
  
  /**
   * Object metadata key for the nanohttpd monitor
   */
  public static final String LEGACY_HTTP_RESPONSE_QUEUE_KEY="legacyHttpResponseQueue";
  
  @XmlTransient
  protected HttpServer server;
  
  private int port;
  private String host;
  private int requestBacklog;
  
  public LegacyHttpConnection() {
    setPort(DEFAULT_SERVER_PORT);
    setHost(DEFAULT_SERVER_HOST);
    setRequestBacklog(DEFAULT_REQUEST_BACKLOG);
  }

  @Override
  public void addMessageConsumer(AdaptrisMessageConsumer consumer) throws CoreException {
    super.addMessageConsumer(consumer);
  }
  
  @Override
  protected void prepareConnection() throws CoreException {
    log.info("Preparing HTTP server");
    try {
      server = HttpServer.create(new InetSocketAddress(getHost(), getPort()), getRequestBacklog());
    } catch (IOException e) {
      throw new CoreException("Failed to create HTTP server", e);
    }
    
    log.info("Setting up routes");
    for (AdaptrisMessageConsumer consumer : super.retrieveMessageConsumers()) {
      RequestHandler handler = new RequestHandler((LegacyHttpConsumer)consumer);      
      HttpContext context = server.createContext(consumer.getDestination().getDestination(), handler);
    }
  }

  @Override
  protected void initConnection() throws CoreException {
  }

  @Override
  protected void startConnection() throws CoreException {
    try {
      server.start();
    } catch (Exception e) {
      throw new CoreException("Failed to start http server on [" + getHost() + ":" + getPort() + "]", e);
    }
  }

  @Override
  protected void stopConnection() {
    server.stop(5);
  }

  @Override
  protected void closeConnection() {
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getRequestBacklog() {
    return requestBacklog;
  }

  public void setRequestBacklog(int requestBacklog) {
    this.requestBacklog = requestBacklog;
  }
  
}
