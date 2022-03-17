package com.search;

import com.search.configuration.ElasticsearchConfiguration;
import com.search.configuration.IndexingConfiguration;
import com.search.elasticsearch.EsClientProvider;
import com.search.elasticsearch.EsMessageIndex;
import com.search.elasticsearch.EsUtilities;
import com.search.elasticsearch.RestEsClientProvider;
import com.search.groupme.GroupMembershipChecker;
import org.apache.http.HttpHost;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SearchSpringConfig {

  private static final String MESSAGE_INDEX = "messages";

  @Bean
  public EsClientProvider esClientProvider(ElasticsearchConfiguration elasticsearchConfiguration) {
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

  @Bean
  public EsMessageIndex esMessageIndex(EsClientProvider esClientProvider,
                                       ElasticsearchConfiguration elasticsearchConfiguration) {
    return new EsMessageIndex(esClientProvider, MESSAGE_INDEX, elasticsearchConfiguration);
  }

  @Bean
  public GroupMembershipChecker groupMembershipChecker(IndexingConfiguration indexingConfiguration) {
    return new GroupMembershipChecker(indexingConfiguration.getGroupMeApi());
  }
}
