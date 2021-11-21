package com.search.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * TestClientProvider is a provider for tests
 * that simply provides a RestHighLevelClient
 * pointing to localhost:9200
 */
public class TestEsClientProvider implements EsClientProvider {

  private static final Logger LOG = LoggerFactory.getLogger(TestEsClientProvider.class);
  private final RestHighLevelClient client;

  public TestEsClientProvider() {
    client = new RestHighLevelClient(
        RestClient.builder(new HttpHost("localhost", 9200)));
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
