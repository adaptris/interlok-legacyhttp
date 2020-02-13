package com.adaptris.legacyhttp;

import static org.junit.Assert.assertEquals;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.BaseCase;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.PortManager;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;

public class LegacyHttpConnectionTest extends BaseCase {

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  private int port;

  @Before
  public void setUpLegacyHttpConnectionTest() throws Exception {
    port = PortManager.nextUnusedPort(9090);
  }

  @After
  public void tearDownLegacyHttpConnectionTest() throws Exception {
    PortManager.release(port);
  }

  private StandaloneConsumer createConsumer(int port, AdaptrisMessageListener listener) {
    return createConsumer(port, new LegacyHttpConsumer(new ConfiguredConsumeDestination("/test")), listener);
  }

  private StandaloneConsumer createConsumer(int port, LegacyHttpConsumer consumer, AdaptrisMessageListener listener) {
    StandaloneConsumer sc = new StandaloneConsumer();
    sc.setConnection(new HttpLegacyHttpConnection("localhost", port, 1));
    sc.setConsumer(consumer);
    sc.registerAdaptrisMessageListener(listener);
    return sc;
  }

  private AdaptrisMessageListener createListener(MockMessageProducer p) throws Exception {
    LegacyHttpResponseProducer producer = new LegacyHttpResponseProducer(HttpStatus.OK_200);
    final ServiceList list = LifecycleHelper.initAndStart(new ServiceList(Arrays.asList(new Service[]
    {
        new StandaloneProducer(p), new StandaloneProducer(producer)
    })));

    return new AdaptrisMessageListener() {
      @Override
      public void onAdaptrisMessage(AdaptrisMessage msg, java.util.function.Consumer<AdaptrisMessage> s) {
        try {
          list.doService(msg);
        }
        catch (ServiceException e) {
          e.printStackTrace();
        }
      }

      @Override
      public String friendlyName() {
        return "Test Listener";
      }
    };
  }

  @Test
  public void testNoRoute() throws Exception {
    StandaloneConsumer consumer = LifecycleHelper.initAndStart(createConsumer(port, createListener(new MockMessageProducer())));
    try {
      HttpURLConnection client = (HttpURLConnection) new URL("http://localhost:" + port + "/nosuch").openConnection();
      client.setRequestMethod("GET");
      client.setDoOutput(false);
      assertEquals(404, client.getResponseCode());
    }
    finally {
      LifecycleHelper.stopAndClose(consumer);
    }
  }

  @Test
  public void testGet() throws Exception {
    StandaloneConsumer consumer = LifecycleHelper.initAndStart(createConsumer(port, createListener(new MockMessageProducer())));
    try {
      HttpURLConnection client = (HttpURLConnection) new URL("http://localhost:" + port + "/test").openConnection();
      client.setRequestMethod("GET");
      client.setDoOutput(false);
      assertEquals(200, client.getResponseCode());
    }
    finally {
      LifecycleHelper.stopAndClose(consumer);
    }
  }

  @Test
  public void testPost() throws Exception {
    StandaloneConsumer consumer = LifecycleHelper.initAndStart(createConsumer(port, createListener(new MockMessageProducer())));
    try {
      HttpURLConnection client = (HttpURLConnection) new URL("http://localhost:" + port + "/test").openConnection();
      client.setRequestMethod("POST");
      client.setDoOutput(true);
      OutputStream os = client.getOutputStream();
      os.write("DATA".getBytes());
      os.flush();
      os.close();

      Assert.assertEquals(200, client.getResponseCode());

      InputStream is = client.getInputStream();
      byte[] response = new byte[1024];
      int i = is.read(response);

      String responseStr = new String(response, 0, i);
      System.out.println(responseStr);
      Assert.assertEquals("DATA", responseStr);
      is.close();
    }
    finally {
      LifecycleHelper.stopAndClose(consumer);
    }
  }

  @Test
  public void testWithMetadataHeadersAndParams() throws Exception {
    MockMessageProducer producer = new MockMessageProducer();
    LegacyHttpConsumer consumer = new LegacyHttpConsumer(new ConfiguredConsumeDestination("/test"));
    consumer.setHeaderHandler(new MetadataHeaderHandler());
    consumer.setParameterHandler(new MetadataParameterHandler());
    StandaloneConsumer sc = LifecycleHelper.initAndStart(createConsumer(port, consumer, createListener(producer)));
    try {
      HttpURLConnection client = (HttpURLConnection) new URL("http://localhost:" + port + "/test?myParam=myParamValue")
          .openConnection();
      client.addRequestProperty("myHeader", "myValue");
      client.setRequestMethod("GET");
      client.setDoOutput(false);
      assertEquals(200, client.getResponseCode());
      AdaptrisMessage msg = producer.getMessages().get(0);
      assertEquals("myValue", msg.getMetadataValueIgnoreKeyCase("myHeader"));
      assertEquals("myParamValue", msg.getMetadataValueIgnoreKeyCase("myParam"));
    }
    finally {
      LifecycleHelper.stopAndClose(sc);
    }
  }
}
