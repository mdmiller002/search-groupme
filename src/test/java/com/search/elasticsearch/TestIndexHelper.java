package com.search.elasticsearch;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * TestIndexHelper is a static class that provides
 * testing utility methods to operate on Elasticsearch
 * data directly
 */
public class TestIndexHelper {

  public static final String TEST_INDEX = "test_index";
  private static final Logger LOG = LoggerFactory.getLogger(TestIndexHelper.class);

  /**
   * Delete an index from Elasticsearch
   */
  public static void deleteIndex(RestHighLevelClient client, String index) {
    try {
      if (indexExists(client, index)) {
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        if (!response.isAcknowledged()) {
          LOG.warn("Delete index response was unacknowledged: " + response);
        }
      }
    } catch (IOException | ElasticsearchStatusException e) {
      LOG.error("Unable to delete index " + index, e);
    }
  }

  /**
   * Check if the given index exists
   */
  public static boolean indexExists(RestHighLevelClient client, String index) {
    try {
      return client.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
    } catch (IOException e) {
      LOG.error("Unable to get index", e);
    }
    return false;
  }

}
