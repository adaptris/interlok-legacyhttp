package com.adaptris.legacyhttp;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.util.Args;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RequestHandler implements HttpHandler {

  private transient Logger log = LoggerFactory.getLogger(RequestHandler.class);

  private transient LegacyHttpConsumer consumer;

  public RequestHandler(LegacyHttpConsumer consumer) {
    this.consumer = Args.notNull(consumer, "consumer");
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      log.trace("Processing request: " + exchange.getRequestURI().toString());
      AdaptrisMessage msg = null;
      InputStream requestBody = exchange.getRequestBody();
      if (consumer.getEncoder() != null && requestBody.available() > 0) {
        try {
          msg = consumer.getEncoder().readMessage(requestBody);
        } catch (CoreException e) {
          throw new IOException("Failed to decode request body", e);
        }
      } else {
        OutputStream os = null;
        try {
          msg = defaultIfNull(consumer.getMessageFactory()).newMessage();
          os = msg.getOutputStream();
          if (requestBody.available() > 0) {
            IOUtils.copy(requestBody, os);
          }
          os.flush();
        } finally {
          IOUtils.closeQuietly(os);
          IOUtils.closeQuietly(requestBody);
        }
      }

      msg.addMetadata(CoreConstants.JETTY_URI, exchange.getRequestURI().getPath());
      msg.addMetadata(CoreConstants.HTTP_METHOD, exchange.getRequestMethod());

      consumer.getHeaderHandler().handleHeaders(msg, exchange);

      if (exchange.getRequestURI().getQuery() != null) {
        msg.addMetadata(CoreConstants.JETTY_QUERY_STRING, exchange.getRequestURI().getQuery());
        consumer.getParameterHandler().handleParameters(msg, exchange);
      }

      ArrayBlockingQueue<LegacyHttpMonitor> queue = new ArrayBlockingQueue<>(1);
      msg.addObjectHeader(LegacyHttpConnection.LEGACY_HTTP_RESPONSE_QUEUE_KEY, queue);

      consumer.retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);

      LegacyHttpMonitor monitor = null;
      try {
        monitor = queue.poll(consumer.getRequestTimeout().getInterval(), consumer.getRequestTimeout().getUnit());
      } catch (InterruptedException e) {
      }

      if (monitor == null || monitor.status == null || monitor.result == null) {
        final String message = "Did not receive a result in the configured time window";
        exchange.sendResponseHeaders(HttpStatus.INTERNAL_ERROR_500.getStatusCode(), message.length());
        final OutputStream os = exchange.getResponseBody();
        os.write(message.getBytes());
        os.flush();
      } else {
        Headers headers = exchange.getResponseHeaders();
        for (MetadataElement md : monitor.result.getMetadata()) {
          headers.add(md.getKey(), md.getValue());
        }
        exchange.sendResponseHeaders(monitor.status, monitor.result.getSize());
        if (monitor.result.getSize() > 0) {
          final OutputStream os = exchange.getResponseBody();
          final InputStream is = monitor.result.getInputStream();
          try {
            IOUtils.copy(is, os);
            os.flush();
          } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
          }
        }
      }

    } finally {
      exchange.close();
    }
  }

}
