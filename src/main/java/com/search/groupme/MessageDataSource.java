package com.search.groupme;

import com.search.jsonModels.Group;
import com.search.jsonModels.Message;

import java.util.List;
import java.util.Optional;

public interface MessageDataSource {
  /**
   * Get a message batch of 100 messages, with no special query types. Just returns
   * the first 100 messages in the group in descending order.
   * @param groupId The group to get the messages from.
   * @return An optional list of messages received from the batch call.
   */
  Optional<List<Message>> getMessageBatch(String groupId);

  /**
   * Get a message batch of 100 messages, using the query type specified
   * @param groupId The group to get the messages from
   * @param type The message query type, corresponds to GroupMe API's query parameters
   * @param messageId The message ID to get messages before/after/since for
   * @return An optional list of messages received from the batch call
   */
  Optional<List<Message>> getMessageBatch(String groupId, MessageQueryType type, String messageId);

  /**
   * Get all groups that the user this interface corresponds to belongs to.
   * @return A list of all groups. List may be empty.
   */
  List<Group> getAllGroups();
}
