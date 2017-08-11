package com.adaptris.legacyhttp;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.google.common.base.Splitter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RequestHandler implements HttpHandler {

  Log log = LogFactory.getLog(RequestHandler.class);

  private LegacyHttpConsumer consumer;

  public RequestHandler(LegacyHttpConsumer consumer) {
    if (consumer == null)
      throw new NullPointerException("NanoHttpdConsumer may not be null");
    this.consumer = consumer;
  }

  // protected Response processRequest(Map<String, String> urlParams,
  // IHTTPSession session) {
  // try {
  // AdaptrisMessage msg = null;
  // log.debug("Request headers:" + session.getHeaders());
  // if (consumer.getEncoder() != null) {
  // msg = consumer.getEncoder().readMessage(session.getInputStream());
  // } else {
  // OutputStream os = null;
  // InputStream is = null;
  // try {
  // msg = defaultIfNull(consumer.getMessageFactory()).newMessage();
  // os = msg.getOutputStream();
  // is = session.getInputStream();
  // if(is.available() > 0) {
  // IOUtils.copy(is, os);
  // }
  // os.flush();
  // } finally {
  // IOUtils.closeQuietly(os);
  // IOUtils.closeQuietly(is);
  // }
  // }
  //
  // msg.addMetadata(CoreConstants.JETTY_URI, session.getUri());
  // msg.addMetadata(CoreConstants.HTTP_METHOD, session.getMethod().toString());
  //
  // if(session.getQueryParameterString() != null)
  // msg.addMetadata(CoreConstants.JETTY_QUERY_STRING,
  // session.getQueryParameterString());
  //
  // for (Entry<String, String> header : session.getHeaders().entrySet()) {
  // msg.addMetadata("header." + header.getKey(), header.getValue());
  // }
  // for (Entry<String, String> param : urlParams.entrySet()) {
  // msg.addMetadata("params." + param.getKey(), param.getValue());
  // }
  //
  // ArrayBlockingQueue<NanoHttpdMonitor> queue = new ArrayBlockingQueue<>(1);
  // msg.addObjectHeader(NanoHttpdConnection.NANO_HTTPD_SESSION_KEY, queue);
  //
  //
  // consumer.retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);
  //
  // NanoHttpdMonitor monitor = null;
  // try {
  // monitor = queue.poll(consumer.getRequestTimeout().getInterval(),
  // consumer.getRequestTimeout().getUnit());
  // } catch (InterruptedException e) {
  // }
  //
  // if (monitor == null) {
  // monitor = new NanoHttpdMonitor();
  // log.error("No response received");
  // monitor.status = Status.INTERNAL_ERROR;
  // }
  //
  // if (monitor.status == null) {
  // log.error("No status for response");
  // monitor.status = Status.INTERNAL_ERROR;
  // }
  //
  // Response r;
  // if (monitor.result == null) {
  // log.error("No message for response");
  // r = NanoHTTPD.newFixedLengthResponse(monitor.status, "text/plain",
  // "Unexpected exception");
  // } else if(msg.getSize() > 0) {
  // r = NanoHTTPD.newChunkedResponse(monitor.status,
  // monitor.result.getMetadataValueIgnoreKeyCase(HttpConstants.CONTENT_TYPE),
  // monitor.result.getInputStream());
  // } else {
  // r = NanoHTTPD.newFixedLengthResponse(monitor.status, "text/plain", "");
  // }
  // return r;
  //
  // } catch (Exception e) {
  // log.error("Exception processing NanoHTTPD request", e);
  // return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR,
  // "text/plain", e.getMessage());
  // }
  // }

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

      for (Entry<String, List<String>> header : exchange.getRequestHeaders().entrySet()) {
        if (header.getValue().size() > 1) {
          for (int i = 0; i < header.getValue().size(); i++) {
            msg.addMetadata("header." + header.getKey() + "." + i, header.getValue().get(i));
          }
        } else if (!header.getValue().isEmpty()) {
          msg.addMetadata("header." + header.getKey(), header.getValue().get(0));
        }
      }

      if (exchange.getRequestURI().getQuery() != null) {
        msg.addMetadata(CoreConstants.JETTY_QUERY_STRING, exchange.getRequestURI().getQuery());
        Map<String, String> params = Splitter.on("&").trimResults().withKeyValueSeparator("=")
            .split(exchange.getRequestURI().getQuery());
        for (Entry<String, String> param : params.entrySet()) {
          msg.addMetadata("params." + param.getKey(), param.getValue());
        }
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
