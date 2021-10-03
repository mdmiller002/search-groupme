package com.search;

import com.search.configuration.ElasticsearchConfiguration;
import com.search.elasticsearch.EsUtilities;
import com.search.elasticsearch.RestClientManager;
import org.apache.http.HttpHost;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SearchSpringConfig {

  @Bean
  public RestClientManager restClientManager(ElasticsearchConfiguration elasticsearchConfiguration) {
    List<HttpHost> hosts = new ArrayList<>();
    for (String host : elasticsearchConfiguration.getHosts()) {
      hosts.add(new HttpHost(host, 9200));
    }
    return new RestClientManager(hosts);
  }

  @Bean
  public EsUtilities esUtilities(RestClientManager restClientManager) {
    return new EsUtilities(restClientManager);
  }

}
