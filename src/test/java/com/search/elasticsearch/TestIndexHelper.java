package com.search.elasticsearch;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

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
          LOG.warn("Delete index response was unacknowledged: {}", response);
        }
      } else {
        LOG.info("Index [{}] does not exist -- not deleting", index);
      }
    } catch (IOException | ElasticsearchStatusException e) {
      LOG.error("Unable to delete index {}", index, e);
    }
  }

  /**
   * Delete all data from the specified index
   */
  public static void deleteDataFromIndex(RestHighLevelClient client, String index) {
    LOG.info("Deleting data from index {}", index);
    try {
      if (!indexExists(client, index)) {
        LOG.info("Attempting to delete from an index which doesn't exist, returning");
        return;
      }
      DeleteByQueryRequest request = new DeleteByQueryRequest(index);
      request.setQuery(QueryBuilders.matchAllQuery());
      request.setRefresh(true);
      client.deleteByQuery(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
      LOG.error("Unable to delete by query", e);
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

  /**
   * Get the number of search hits when searching the provided index
   * for the specified terms. Search terms are passed through a match query
   * Return the number of search hits retrieved by the query
   */
  public static long numSearchHitsWithSearchTerms(RestHighLevelClient client, String index, List<Pair<String, Object>> terms) {
    if (terms == null || terms.size() <= 0) {
      LOG.warn("No search terms provided to check if document exists in Index");
      return 0;
    }
    try {
      SearchRequest searchRequest = new SearchRequest(index);
      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
      BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
      for (Pair<String, Object> term : terms) {
        boolQueryBuilder.must(QueryBuilders.matchQuery(term.getValue0(), term.getValue1()));
      }
      searchSourceBuilder.query(boolQueryBuilder);
      searchRequest.source(searchSourceBuilder);
      SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
      return searchResponse.getHits().getTotalHits().value;
    } catch (IOException e) {
      LOG.error("Unable to search on index {}", index, e);
    }
    return 0;
  }

  /**
   * Determine if a document with the specified doc ID exists in the index
   */
  public static boolean doesDocExistById(RestHighLevelClient client, String index, String docId) {
    try {
      GetRequest getRequest = new GetRequest(index, docId);
      GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
      return getResponse.isExists() && index.equals(getResponse.getIndex()) && docId.equals(getResponse.getId());
    } catch (IOException e) {
      LOG.error("Unable to execute get request on index {}", index, e);
    }
    return false;
  }

}
