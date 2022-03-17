package com.search;

import com.search.configuration.ElasticsearchConfiguration;
import com.search.elasticsearch.EsClientProvider;
import com.search.elasticsearch.EsMessageIndex;
import com.search.elasticsearch.EsUtilities;
import com.search.elasticsearch.RestEsClientProvider;
import com.search.groupme.GroupMembershipChecker;
import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

import static com.search.configuration.ConfigConstants.GROUP_ME_API_KEY;

@Configuration
public class SearchSpringConfig {

  private static final String MESSAGE_INDEX = "messages";

  @Value("${" + GROUP_ME_API_KEY + "}")
  private String groupMeApiEndpoint;

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
  public GroupMembershipChecker groupMembershipChecker() {
    return new GroupMembershipChecker(groupMeApiEndpoint);
  }
}
