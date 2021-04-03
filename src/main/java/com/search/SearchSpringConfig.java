package com.search;

import com.search.elasticsearch.RestClientManager;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.MainResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SearchSpringConfig {

  private static final Logger LOG = LoggerFactory.getLogger(SearchSpringConfig.class);

  @Bean
  RestClientManager restClientManager() {
    // TODO read from some config or something
    HttpHost host = new HttpHost("localhost", 9200);
    List<HttpHost> hosts = new ArrayList<>();
    hosts.add(host);
    RestClientManager restClientManager = new RestClientManager(hosts);
    try {
      MainResponse mainResponse = restClientManager.get().info(RequestOptions.DEFAULT);
      LOG.info("Cluster: " + mainResponse.getClusterName());
      LOG.info("Version: " + mainResponse.getVersion());
      return restClientManager;
    } catch (IOException e) {
      LOG.error("Unable to get a response from Elasticsearch: ", e);
    }
    return null;
  }

}
