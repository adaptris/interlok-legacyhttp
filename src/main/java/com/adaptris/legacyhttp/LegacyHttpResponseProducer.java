package com.adaptris.legacyhttp;

import java.util.Queue;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.NullConnection;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.http.server.HttpStatusProvider.HttpStatus;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("legacy-http-response-producer")
@ComponentProfile(summary = "Write and commit the HTTP Response", tag = "producer,http,https", recommended ={ NullConnection.class})
@DisplayOrder(order = {  "status", "metadataFilter"})
public class LegacyHttpResponseProducer extends ProduceOnlyProducerImp {
  
  @NotNull
  @Valid
  private HttpStatus status;
  @Valid
  @AutoPopulated
  @NotNull
  private MetadataFilter metadataFilter;
  
  public LegacyHttpResponseProducer() {
    status = HttpStatus.OK_200;
    metadataFilter = new RemoveAllMetadataFilter();
  }

  public LegacyHttpResponseProducer(HttpStatus status) {
    this();
    setStatus(status);
  }

  @Override
  public void init() throws CoreException {
    if(status == null) throw new CoreException("No status provided");
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
  }

  @Override
  public void prepare() throws CoreException {
    
  }

  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
    if(msg.getObjectHeaders().containsKey(LegacyHttpConnection.LEGACY_HTTP_RESPONSE_QUEUE_KEY)) {
      Queue<LegacyHttpMonitor> queue = (Queue<LegacyHttpMonitor>)msg.getObjectHeaders().get(LegacyHttpConnection.LEGACY_HTTP_RESPONSE_QUEUE_KEY);
      LegacyHttpMonitor monitor = new LegacyHttpMonitor();
      try {
        monitor.result = (AdaptrisMessage)msg.clone();
        MetadataCollection md = metadataFilter.filter(monitor.result);
        monitor.result.clearMetadata();
        monitor.result.setMetadata(md.toSet());
      } catch (CloneNotSupportedException e) {
        throw new ProduceException("Failed to clone message", e);
      }
      monitor.status  = getStatus().getStatusCode();
      if(!queue.offer(monitor)) {
        throw new ProduceException("Unable to return Legacy HTTPD response");
      }
    } else {
      throw new ProduceException("Message does not have associated Legacy HTTPD session");
    }
  }

  public HttpStatus getStatus() {
    return status;
  }

  public void setStatus(HttpStatus status) {
    this.status = status;
  }

  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  public void setMetadataFilter(MetadataFilter metadataFilter) {
    this.metadataFilter = metadataFilter;
  }

}
