package com.search.elasticsearch;

import org.elasticsearch.client.RestHighLevelClient;

/**
 * ClientProvider provides an elasticsearch RestHighLevelClient
 * for the application to use and close.
 */
public interface ClientProvider {

  RestHighLevelClient get();

  void close();

}
