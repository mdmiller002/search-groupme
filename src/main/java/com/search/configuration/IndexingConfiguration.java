package com.search.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(IndexingConfiguration.PREFIX)
public class IndexingConfiguration {
  public static final String PREFIX = "indexing";

  private boolean runIndexing = true;
  private String groupMeApi = "localhost:1080";

  public boolean runIndexing() {
    return runIndexing;
  }

  public void setRunIndexing(boolean runIndexing) {
    this.runIndexing = runIndexing;
  }

  public String getGroupMeApi() {
    return groupMeApi;
  }

  public void setGroupMeApi(String groupMeApi) {
    this.groupMeApi = groupMeApi;
  }
}
