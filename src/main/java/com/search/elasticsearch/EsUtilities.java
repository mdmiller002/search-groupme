package com.search.elasticsearch;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.MainResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EsUtilities {

  private static final Logger LOG = LoggerFactory.getLogger(EsUtilities.class);

  private final EsClientProvider esClientProvider;

  public EsUtilities(EsClientProvider esClientProvider) {
    this.esClientProvider = esClientProvider;
  }

  public boolean isEsReachable() {
    try {
      MainResponse response = esClientProvider.get().info(RequestOptions.DEFAULT);
      LOG.info("Successfully pinged Elasticsearch cluster, cluster information:");
      LOG.info("Cluster: {}", response.getClusterName());
      LOG.info("Version: {}", response.getVersion().getNumber());
      return true;
    } catch (ElasticsearchException | IOException e) {
      LOG.error("Unable to reach Elasticsearch server.", e);
      return false;
    }
  }

}
