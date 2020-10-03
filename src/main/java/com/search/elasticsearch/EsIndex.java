package com.search.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * EsIndex represents an index and all the operations you can execute on an
 * index, like persisting data and searching in the index.
 * EsIndex is the top level class to Elasticsearch. All Elasticsearch
 * calls should go through this class.
 */
public class EsIndex {

  private static final Logger LOG = LoggerFactory.getLogger(EsIndex.class);

  private final ClientProvider clientProvider;
  private final String indexName;

  public EsIndex(ClientProvider clientProvider, String indexName) {
    this.clientProvider = clientProvider;
    this.indexName = indexName;
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
    IndexRequest indexRequest = new IndexRequest(indexName)
        .source(messageJson, XContentType.JSON);
    try {
      clientProvider.get().index(indexRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      LOG.error("Unable to execute index request " + indexRequest, e);
    }
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
}
