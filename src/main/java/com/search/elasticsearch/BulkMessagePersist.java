package com.search.elasticsearch;

import com.search.jsonModels.Message;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Encapsulates one instance of a bulk index operation.
 * Can be created by clients and passed to the EsMessageIndex to execute
 * the bulk persist
 */
public class BulkMessagePersist {

  private static final Logger LOG = LoggerFactory.getLogger(BulkMessagePersist.class);

  private final String index;
  private final BulkRequest bulkRequest;

  public BulkMessagePersist(String index) {
    bulkRequest = new BulkRequest();
    this.index = index;
  }

  /**
   * Get the Elasticsearch BulkRequest contained in the current Bulk Persist
   */
  public BulkRequest getBulkRequest() {
    return bulkRequest;
  }

  /**
   * Get the total number of requests in the bulk request
   */
  public int getNumMessages() {
    return bulkRequest.numberOfActions();
  }

  /**
   * Add a message to the bulk persist operation
   */
  public void addMessage(Message message) {
    if (message == null) {
      LOG.warn("Unable to add null message to bulk request");
      return;
    }
    LOG.debug("Adding message [{}] to bulk request", message);
    Optional<String> docIdOptional = message.getDocId();
    if (docIdOptional.isEmpty()) {
      LOG.warn("Unable to create doc ID for message");
      return;
    }

    String docId = docIdOptional.get();
    Optional<String> messageStrOptional = EsMessageIndex.messageToJson(message);
    if (messageStrOptional.isPresent()) {
      addMessage0(docId, messageStrOptional.get());
    } else {
      LOG.warn("Message unable to be converted to JSON -- not adding to bulk [{}]", message);
    }
  }

  private void addMessage0(String docId, String messageJson) {
    IndexRequest indexRequest = new IndexRequest(index)
        .id(docId)
        .source(messageJson, XContentType.JSON);
    bulkRequest.add(indexRequest);
  }

}
