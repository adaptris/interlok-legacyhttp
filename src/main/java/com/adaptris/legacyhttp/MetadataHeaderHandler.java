package com.adaptris.legacyhttp;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.adaptris.core.AdaptrisMessage;
import com.sun.net.httpserver.HttpExchange;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link com.adaptris.core.http.server.HeaderHandler} implementation that stores HTTP headers as standard metadata.
 * 
 * @config legacy-http-headers-as-metadata
 * 
 */
@XStreamAlias("legacy-http-headers-as-metadata")
public class MetadataHeaderHandler extends HeaderHandlerImpl {

  public MetadataHeaderHandler() {
  }

  @Override
  protected void handleHeaders(AdaptrisMessage msg, HttpExchange exchange, String itemPrefix) {
    String prefix = defaultIfEmpty(itemPrefix, "");
    for (Entry<String, List<String>> header : exchange.getRequestHeaders().entrySet()) {
      StringBuilder value = new StringBuilder();
      for (Iterator<String> i = header.getValue().iterator(); i.hasNext();) {
        value.append(i.next());
        if (i.hasNext()) {
          value.append(valueSeparator());
        }
      }
      if (StringUtils.isNotBlank(value.toString())) {
        String metadataKey = prefix + header.getKey();
        log.trace("Adding Metadata [{}: {}]", metadataKey, value);
        msg.addMetadata(metadataKey, value.toString());
      }
    }
  }


}
