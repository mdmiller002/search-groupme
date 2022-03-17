package com.search.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.configuration.ElasticsearchConfiguration;
import com.search.jsonModels.EsMessageDocument;
import com.search.jsonModels.Message;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
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
  private final long maxIndexSizeInBytes;
  private final int persistSpaceCheckInterval;

  private RefreshPolicy refreshPolicy = RefreshPolicy.NONE;
  private int numPersistsUntilSizeCheck;

  public EsMessageIndex(EsClientProvider esClientProvider, String index, ElasticsearchConfiguration configuration) {
    this.client = esClientProvider.get();
    this.index = index;
    maxIndexSizeInBytes = (long) configuration.getMaxIndexSizeGb() * 1024 * 1024 * 1024;
    persistSpaceCheckInterval = configuration.getPersistSpaceCheckInterval();
    numPersistsUntilSizeCheck = persistSpaceCheckInterval;
  }

  public String getIndex() {
    return index;
  }

  /**
   * Set a custom refresh policy for indexing documents into this index.
   * This can be useful to set custom refresh policies during testing
   * to assert that data was indexed.
   */
  public void setRefreshPolicy(RefreshPolicy refreshPolicy) {
    this.refreshPolicy = refreshPolicy;
  }

  public void createIndex() {
    try {
      if (!indexExists()) {
        Map<String, Object> mapping = getMapping();
        LOG.info("Creating new index {} with mapping {}", index, mapping);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
        createIndexRequest.mapping(mapping);
        client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
      }
    } catch (IOException e) {
      LOG.error("Failed to create index {}", index, e);
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
    id.put("type", "keyword");
    Map<String, Object> groupId = new HashMap<>();
    groupId.put("type", "keyword");
    Map<String, Object> nameKeyword = new HashMap<>();
    nameKeyword.put("type", "keyword");
    Map<String, Object> name = new HashMap<>();
    name.put("type", "text");
    Map<String, Object> text = new HashMap<>();
    text.put("type", "text");

    Map<String, Object> properties = new HashMap<>();
    properties.put(Message.ID_KEY, id);
    properties.put(Message.GROUP_ID_KEY, groupId);
    properties.put(Message.NAME_KEY, name);
    properties.put(Message.NAME_KEYWORD_KEY, nameKeyword);
    properties.put(Message.TEXT_KEY, text);
    Map<String, Object> mapping = new HashMap<>();
    mapping.put("properties", properties);
    return mapping;
  }

  public void executeBulkPersist(BulkMessagePersist bulkMessagePersist) {
    if (bulkMessagePersist == null) {
      LOG.warn("Unable to execute null bulk request");
      return;
    }
    if (maxIndexSizeInBytes > 0) {
      numPersistsUntilSizeCheck--;
      if (numPersistsUntilSizeCheck <= 0) {
        numPersistsUntilSizeCheck = persistSpaceCheckInterval;
        try {
          if (isIndexOutOfSpace()) {
            LOG.info("Index {} is out of configured space, not persisting any more data", index);
            return;
          }
        } catch (IOException e) {
          LOG.error("Error when checking size of index, could not check size so exiting", e);
          return;
        }
      }
    }
    BulkRequest bulkRequest = bulkMessagePersist.getBulkRequest();
    bulkRequest.setRefreshPolicy(refreshPolicy);
    try {
      client.bulk(bulkRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      LOG.error("Unable to execute bulk request", e);
    }
  }

  private boolean isIndexOutOfSpace() throws IOException {
    long actualSize = getSizeOfIndex();
    if (actualSize > maxIndexSizeInBytes) {
      LOG.info("Attempting to persist when size of index ({}) is larger than max allowed size ({})",
          actualSize, maxIndexSizeInBytes);
      return true;
    }
    return false;
  }

  private long getSizeOfIndex() throws IOException {
    Response response = client.getLowLevelClient().performRequest(new Request("GET", index + "/_stats"));
    JsonNode body = new ObjectMapper().readTree(response.getEntity().getContent());
    LOG.debug("Response from {}/_stats: {}", index, body);
    return body
        .get("indices")
        .get(index)
        .get("primaries")
        .get("store")
        .get("size_in_bytes")
        .asLong(0);
  }

  /**
   * Search for a given message in the message index and return a list of results.
   * Will attempt to search by both message name and message text.
   * Search using generic match queries
   */
  public List<Message> searchForMessage(Message message) {
    LOG.debug("Searching for message [{}]", message);
    if (message == null) {
      return Collections.emptyList();
    }
    if (message.getGroupId() == null) {
      LOG.debug("Searching for message [{}] with null group ID - returning null", message);
      return Collections.emptyList();
    }
    List<Pair<String, Object>> searchTerms = new ArrayList<>();
    if (message.getName() != null) {
      searchTerms.add(new Pair<>(Message.NAME_KEY, message.getName()));
    }
    if (message.getText() != null) {
      searchTerms.add(new Pair<>(Message.TEXT_KEY, message.getText()));
    }
    LOG.debug("Using search terms {}", searchTerms);
    return executeSearch(message.getGroupId(), searchTerms);
  }

  private List<Message> executeSearch(String groupId, List<Pair<String, Object>> searchTerms) {
    SearchRequest searchRequest = prepareSearchRequest(groupId, searchTerms);
    try {
      SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
      return processSearchResponse(searchResponse);
    } catch (IOException e) {
      LOG.error("Unable to search on index {}", index, e);
    }
    return Collections.emptyList();
  }

  private SearchRequest prepareSearchRequest(String groupId, List<Pair<String, Object>> searchTerms) {
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

  public static Optional<String> messageToJson(Message message) {
    if (message == null) {
      return Optional.empty();
    }
    try {
      ObjectMapper mapper = new ObjectMapper();
      EsMessageDocument esMessage = new EsMessageDocument(message);
      return Optional.of(mapper.writeValueAsString(esMessage));
    } catch (JsonProcessingException e) {
      LOG.error("Unable to convert message to string. Message: {}", message, e);
    }
    return Optional.empty();
  }

  public static Optional<Message> jsonToMessage(String json) {
    if (json == null || json.length() <= 0) {
      return Optional.empty();
    }
    try {
      ObjectMapper mapper = new ObjectMapper();
      EsMessageDocument esMessage = mapper.readValue(json, EsMessageDocument.class);
      return Optional.of(new Message(
          esMessage.getId(), esMessage.getGroupId(), esMessage.getName(), esMessage.getText()));
    } catch (JsonProcessingException e) {
      LOG.error("Unable to convert JSON string to message. JSON: {}", json, e);
    }
    return Optional.empty();
  }
}
