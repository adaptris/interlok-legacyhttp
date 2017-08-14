package com.adaptris.legacyhttp;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.adaptris.core.AdaptrisMessage;
import com.sun.net.httpserver.HttpExchange;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link com.adaptris.core.http.server.ParameterHandler} implementation that stores headers as standard metadata.
 * 
 * @config jetty-http-parameters-as-metadata
 * 
 */
@XStreamAlias("legacy-http-parameters-as-metadata")
public class MetadataParameterHandler extends ParameterHandlerImpl {

  private static final String ENCODING = "UTF-8";
  public MetadataParameterHandler() {
  }

  protected void handleParameters(AdaptrisMessage msg, HttpExchange exchange, String itemPrefix) {
    String prefix = defaultIfEmpty(itemPrefix, "");
    Map<String, String> params = splitQuery(exchange.getRequestURI());
    for (Entry<String, String> param : params.entrySet()) {
      msg.addMetadata(itemPrefix + param.getKey(), param.getValue());
    }
  }

  private static Map<String, String> splitQuery(URI uri) {
    Map<String, String> result = new LinkedHashMap<String, String>();
    try {
      String query = uri.getRawQuery();
      String[] pairs = query.split("&");
      for (String pair : pairs) {
        int idx = pair.indexOf("=");
        result.put(URLDecoder.decode(pair.substring(0, idx), ENCODING), URLDecoder.decode(pair.substring(idx + 1), ENCODING));
      }
    }
    catch (UnsupportedEncodingException e) {
      // Really UTF-8 isn't supported?
      throw new RuntimeException(e);
    }
    return result;
  }

}
