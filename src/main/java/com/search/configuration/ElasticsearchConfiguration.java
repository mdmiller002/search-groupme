package com.search.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(ElasticsearchConfiguration.PREFIX)
public class ElasticsearchConfiguration {
  public static final String PREFIX = "elasticsearch";

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
