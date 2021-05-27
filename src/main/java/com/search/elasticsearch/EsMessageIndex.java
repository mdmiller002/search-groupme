package com.search.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.jsonModels.Message;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * EsIndex represents an index and all the operations you can execute on an
 * index, like persisting data and searching in the index.
 * EsIndex is the top level class to Elasticsearch. All Elasticsearch
 * calls should go through this class.
 */
public class EsMessageIndex {

  private static final Logger LOG = LoggerFactory.getLogger(EsMessageIndex.class);

  private final RestHighLevelClient client;
  private final String index;

  private WriteRequest.RefreshPolicy refreshPolicy = WriteRequest.RefreshPolicy.NONE;

  public EsMessageIndex(ClientProvider clientProvider, String index) {
    this.client = clientProvider.get();
    this.index = index;
    createIndex();
  }

  /**
   * Set a custom refresh policy for indexing documents into this index.
   * This can be useful to set custom refresh policies during testing
   * to assert that data was indexed.
   */
  public void setRefreshPolicy(WriteRequest.RefreshPolicy refreshPolicy) {
    this.refreshPolicy = refreshPolicy;
  }

  private void createIndex() {
    try {
      if (!indexExists()) {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
        createIndexRequest.mapping(getMapping());
        client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
      }
    } catch (IOException e) {
      LOG.error("Failed to create index " + index, e);
    }
  }

  private boolean indexExists() {
    try {
      return client.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
    } catch (IOException e) {
      LOG.error("Failed to check if index exists", e);
      return false;
    }
  }

  private Map<String, Object> getMapping() {
    Map<String, Object> id = new HashMap<>();
    id.put("type", "long");
    Map<String, Object> groupId = new HashMap<>();
    groupId.put("type", "long");
    Map<String, Object> name = new HashMap<>();
    name.put("type", "text");
    Map<String, Object> text = new HashMap<>();
    text.put("type", "text");

    Map<String, Object> properties = new HashMap<>();
    properties.put(Message.ID_KEY, id);
    properties.put(Message.GROUP_ID_KEY, groupId);
    properties.put(Message.NAME_KEY, name);
    properties.put(Message.TEXT_KEY, text);
    Map<String, Object> mapping = new HashMap<>();
    mapping.put("properties", properties);
    return mapping;
  }

  /**
   * Persist a message into Elasticsearch at the index
   * specified at indexName
   * @param message message to persist
   */
  public void persistMessage(Message message) {
    Optional<String> messageStrOptional = messageToJson(message);
    if (messageStrOptional.isPresent()) {
      executePersist(messageStrOptional.get());
    } else {
      LOG.warn("Message unable to be converted to JSON -- not persisting message " + message);
    }
  }

  private void executePersist(String messageJson) {
    IndexRequest indexRequest = new IndexRequest(index)
        .source(messageJson, XContentType.JSON)
        .setRefreshPolicy(refreshPolicy);
    try {
      client.index(indexRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      LOG.error("Unable to execute index request " + indexRequest, e);
    }
  }

  /**
   * Search for a given message in the message index and return a list of results.
   * Will attempt to search by both message name and message text.
   * Search using generic match queries
   */
  public List<Message> searchForMessage(Message message) {
    if (message == null) {
      return Collections.emptyList();
    }
    if (message.getGroupId() == null) {
      LOG.debug("Searching for message [" + message + "] with null group ID - returning null");
      return Collections.emptyList();
    }
    List<Pair<String, Object>> searchTerms = new ArrayList<>();
    if (message.getName() != null) {
      searchTerms.add(new Pair<>(Message.NAME_KEY, message.getName()));
    }
    if (message.getText() != null) {
      searchTerms.add(new Pair<>(Message.TEXT_KEY, message.getText()));
    }
    return executeSearch(message.getGroupId(), searchTerms);
  }

  private List<Message> executeSearch(long groupId, List<Pair<String, Object>> searchTerms) {
    SearchRequest searchRequest = prepareSearchRequest(groupId, searchTerms);
    try {
      SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
      return processSearchResponse(searchResponse);
    } catch (IOException e) {
      LOG.error("Unable to search on index " + index, e);
    }
    return Collections.emptyList();
  }

  private SearchRequest prepareSearchRequest(long groupId, List<Pair<String, Object>> searchTerms) {
    SearchRequest searchRequest = new SearchRequest(index);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
    boolQueryBuilder.must(QueryBuilders.termQuery(Message.GROUP_ID_KEY, groupId));
    for (Pair<String, Object> term : searchTerms) {
      boolQueryBuilder.must(QueryBuilders.matchQuery(term.getValue0(), term.getValue1()));
    }
    searchSourceBuilder.query(boolQueryBuilder);
    searchRequest.source(searchSourceBuilder);
    return searchRequest;
  }

  private List<Message> processSearchResponse(SearchResponse response) {
    if (response == null || response.getHits().getTotalHits().value <= 0) {
      return Collections.emptyList();
    }
    List<Message> messages = new ArrayList<>();
    for (SearchHit hit : response.getHits()) {
      String source = hit.getSourceAsString();
      Optional<Message> messageOptional = jsonToMessage(source);
      messageOptional.ifPresent(messages::add);
    }
    return messages;
  }

  protected Optional<String> messageToJson(Message message) {
    if (message == null) {
      return Optional.empty();
    }
    try {
      ObjectMapper mapper = new ObjectMapper();
      return Optional.of(mapper.writeValueAsString(message));
    } catch (JsonProcessingException e) {
      LOG.error("Unable to convert message to string. Message: " + message, e);
    }
    return Optional.empty();
  }

  protected Optional<Message> jsonToMessage(String json) {
    if (json == null || json.length() <= 0) {
      return Optional.empty();
    }
    try {
      ObjectMapper mapper = new ObjectMapper();
      return Optional.of(mapper.readValue(json, Message.class));
    } catch (JsonProcessingException e) {
      LOG.error("Unable to convert JSON string to message. JSON: " + json, e);
    }
    return Optional.empty();
  }
}
