package com.search.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.List;

public class RestClientManager implements ClientProvider {

  RestHighLevelClient client;

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
      e.printStackTrace();
    }
  }

}
