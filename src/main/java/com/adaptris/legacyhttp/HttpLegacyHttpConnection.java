package com.adaptris.legacyhttp;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.CoreException;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Lightweight HTTP server using {@link HttpServer}
 * <p>
 * Included to provide a mechanism for implementing HTTP services where Java 1.8 isn't available and you really don't want to use
 * Jetty. It is not as fully featured
 * </p>
 * 
 * @author ellidges
 */
@XStreamAlias("http-legacy-http-connection")
@ComponentProfile(summary = "Lightweight HTTP server", tag = "connections,http")
@DisplayOrder(order ={"host", "port", "requestBacklog"})
public class HttpLegacyHttpConnection extends LegacyHttpConnection {

  public HttpLegacyHttpConnection() {
    super();
  }

  public HttpLegacyHttpConnection(String host, Integer port, Integer backlog) {
    this();
    setHost(host);
    setPort(port);
    setRequestBacklog(backlog);
  }

  @Override
  // Do this at the very last minute, we should be able to do it during prepare, but sometimes back-refs aren't done
  // in time (StandaloneConsumer doesn't set back-refs until init, foolishly).
  protected void initConnection() throws CoreException {
    log.debug("Initialising HTTP server");
    try {
      server = HttpServer.create(new InetSocketAddress(host(), port()), requestBacklog());
    }
    catch (IOException e) {
      throw new CoreException("Failed to create HTTP server", e);
    }

    log.trace("Setting up routes");
    for (AdaptrisMessageConsumer consumer : super.retrieveMessageConsumers()) {
      RequestHandler handler = new RequestHandler((LegacyHttpConsumer) consumer);
      HttpContext context = server.createContext(consumer.getDestination().getDestination(), handler);
    }
  }
}
