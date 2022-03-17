package com.search.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(ElasticsearchConfiguration.DOMAIN)
public class ElasticsearchConfiguration {
  public static final String DOMAIN = "elasticsearch";

  private List<String> hosts;
  private int  maxIndexSizeGb = 5;
  private int persistSpaceCheckInterval = 10;

  public List<String> getHosts() {
    return hosts;
  }

  public void setHosts(List<String> hosts) {
    this.hosts = hosts;
  }

  public int getMaxIndexSizeGb() {
    return maxIndexSizeGb;
  }

  public void setMaxIndexSizeGb(int maxIndexSizeGb) {
    this.maxIndexSizeGb = maxIndexSizeGb;
  }

  public int getPersistSpaceCheckInterval() {
    return persistSpaceCheckInterval;
  }

  public void setPersistSpaceCheckInterval(int persistSpaceCheckInterval) {
    this.persistSpaceCheckInterval = persistSpaceCheckInterval;
  }
}
