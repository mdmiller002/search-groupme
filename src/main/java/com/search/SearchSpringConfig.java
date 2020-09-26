package com.search;

import com.search.elasticsearch.RestClientManager;
import org.apache.http.HttpHost;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SearchSpringConfig {

  @Bean
  RestClientManager restClientManager() {
    // TODO read from some config or something
    HttpHost host = new HttpHost("localhost", 9200);
    List<HttpHost> hosts = new ArrayList<>();
    hosts.add(host);
    return new RestClientManager(hosts);
  }

}
