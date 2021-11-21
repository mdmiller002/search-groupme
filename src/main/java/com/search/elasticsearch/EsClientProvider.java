package com.search.elasticsearch;

import org.elasticsearch.client.RestHighLevelClient;

/**
 * EsClientProvider provides an elasticsearch RestHighLevelClient
 * for the application to use and close.
 */
public interface EsClientProvider {

  RestHighLevelClient get();

  void close();

}
