package com.adaptris.legacyhttp;

import static com.adaptris.legacyhttp.LegacyHttpConnection.DEFAULT_REQUEST_BACKLOG;
import static com.adaptris.legacyhttp.LegacyHttpConnection.DEFAULT_SERVER_HOST;
import static com.adaptris.legacyhttp.LegacyHttpConnection.DEFAULT_SERVER_PORT;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConsumerCase;
import com.adaptris.core.StandaloneConsumer;

public class HttpConsumerTest extends ConsumerCase {


  public HttpConsumerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    HttpLegacyHttpConnection connection = new HttpLegacyHttpConnection(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT,
        DEFAULT_REQUEST_BACKLOG);
    LegacyHttpConsumer consumer = new LegacyHttpConsumer(new ConfiguredConsumeDestination("/my/url"));
    StandaloneConsumer result = new StandaloneConsumer(connection, consumer);
    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-LegacyHTTP";
  }
}
