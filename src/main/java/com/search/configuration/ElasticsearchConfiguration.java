package com.search.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.search.configuration.ConfigConstants.ELASTICSEARCH_DOMAIN;

@Component
@ConfigurationProperties(ELASTICSEARCH_DOMAIN)
public class ElasticsearchConfiguration {

  private List<String> hosts;

  public List<String> getHosts() {
    return hosts;
  }

  public void setHosts(List<String> hosts) {
    this.hosts = hosts;
  }
}
