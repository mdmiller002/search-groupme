package com.search.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class RestClientManager implements ClientProvider {

  private static final Logger LOG = LoggerFactory.getLogger(RestClientManager.class);
  private final RestHighLevelClient client;

  public RestClientManager(List<HttpHost> hosts) {
    client = new RestHighLevelClient(RestClient.builder(hosts.toArray(new HttpHost[0])));
  }

  @Override
  public RestHighLevelClient get() {
    return client;
  }

  @Override
  public void close() {
    try {
      client.close();
    } catch (IOException e) {
      LOG.error("Failed to close client", e);
    }
  }

}
