package com.search;

import com.search.configuration.ElasticsearchConfiguration;
import com.search.elasticsearch.EsClientProvider;
import com.search.elasticsearch.EsUtilities;
import com.search.elasticsearch.RestEsClientProvider;
import org.apache.http.HttpHost;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SearchSpringConfig {

  @Bean
  public EsClientProvider clientProvider(ElasticsearchConfiguration elasticsearchConfiguration) {
    List<HttpHost> hosts = new ArrayList<>();
    for (String host : elasticsearchConfiguration.getHosts()) {
      hosts.add(new HttpHost(host, 9200));
    }
    return new RestEsClientProvider(hosts);
  }

  @Bean
  public EsUtilities esUtilities(EsClientProvider esClientProvider) {
    return new EsUtilities(esClientProvider);
  }

}
