package com.adaptris.legacyhttp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;

import org.junit.Assert;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.BaseCase;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;

public class LegacyHttpConnectionTest extends BaseCase {

  public LegacyHttpConnectionTest(java.lang.String testName) {
    super(testName);
  }
  
  LegacyHttpConnection conn;
  int port;

  @Override
  protected void setUp() throws Exception {
    ServerSocket ss = new ServerSocket(0);
    port = ss.getLocalPort();
    ss.close();
    
    conn = new HttpLegacyHttpConnection();
    conn.setHost("localhost");
    conn.setPort(port);
    conn.setRequestBacklog(1);
    
    conn.addMessageConsumer(createConsumer());
    
    conn.prepareConnection();
    conn.start();
  }
  
  private AdaptrisMessageConsumer createConsumer() {
    AdaptrisMessageConsumer consumer = new LegacyHttpConsumer();
    consumer.setDestination(new ConfiguredConsumeDestination("/test"));
    consumer.registerAdaptrisMessageListener(new AdaptrisMessageListener() {
      
      @Override
      public void onAdaptrisMessage(AdaptrisMessage msg) {
        LegacyHttpResponseProducer producer = new LegacyHttpResponseProducer();
        producer.setStatus(HttpStatus.OK_200);
        try {
          producer.produce(msg);
        } catch (ProduceException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      
      @Override
      public String friendlyName() {
        return "Test Listener";
      }
    });
    return consumer;
  }

  @Override
  protected void tearDown() throws Exception {
    conn.stop();
  }

  public void testNoRoute() throws Exception {
    HttpURLConnection client = (HttpURLConnection)new URL("http://localhost:" + port + "/nosuch").openConnection();
    client.setRequestMethod("GET");
    client.setDoOutput(false);
    Assert.assertEquals(404, client.getResponseCode());
  }

  public void testGet() throws Exception {
    HttpURLConnection client = (HttpURLConnection)new URL("http://localhost:" + port + "/test").openConnection();
    client.setRequestMethod("GET");
    client.setDoOutput(false);
    Assert.assertEquals(200, client.getResponseCode());
  }
  
  public void testPost() throws Exception {
    HttpURLConnection client = (HttpURLConnection)new URL("http://localhost:" + port + "/test").openConnection();
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
}
