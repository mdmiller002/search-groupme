package com.search;

import com.google.common.annotations.VisibleForTesting;
import com.search.elasticsearch.BulkMessagePersist;
import com.search.elasticsearch.EsMessageIndex;
import com.search.groupme.GroupMeInterface;
import com.search.jsonModels.Group;
import com.search.jsonModels.Message;
import com.search.rdbms.hibernate.models.GroupEntity;
import com.search.rdbms.hibernate.repositories.GroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.search.groupme.GroupMeInterface.MessageQueryType.BEFORE_ID;

/**
 * MessageIndexer is responsible for executing the message indexing algorithm
 * to durably and consistently retrieve messages from GroupMe and index them into
 * Elasticsearch.
 */
public class MessageIndexer {

  private static final Logger LOG = LoggerFactory.getLogger(MessageIndexer.class);

  private final GroupMeInterface groupMeInterface;
  private final EsMessageIndex esMessageIndex;
  private final GroupRepository groupRepository;

  private int numMessagesPersisted;

  public MessageIndexer(GroupMeInterface groupMeInterface, EsMessageIndex esMessageIndex, GroupRepository groupRepository) {
    this.groupMeInterface = groupMeInterface;
    this.esMessageIndex = esMessageIndex;
    this.groupRepository = groupRepository;
  }

  protected void updateGroups() {
    numMessagesPersisted = 0;
    List<Group> groups = groupMeInterface.getAllGroups();
    for (Group group : groups) {
      LOG.debug("Running update algorithm for group " + group);
      if (!groupRepository.existsById(group.getId())) {
        LOG.debug("Group not indexed yet, creating new entry in RDBMS");
        GroupEntity groupEntity = new GroupEntity(group.getId(), null, null, false);
        groupRepository.save(groupEntity);
      }
      runUpdateAlgorithm(group.getId());
    }
  }

  private void runUpdateAlgorithm(String groupId) {
    Optional<GroupEntity> groupEntityOptional = groupRepository.findById(groupId);
    if (groupEntityOptional.isEmpty()) {
      LOG.error("Group ID " + groupId + " not found in RDBMS");
      return;
    }
    GroupEntity groupEntity = groupEntityOptional.get();

    if (!groupEntity.getInitialized()) {
      initialGroupIndex(groupEntity);
    } else {
      indexUpward(groupEntity);
    }
  }

  private void initialGroupIndex(GroupEntity groupEntity) {
    String groupId = groupEntity.getId();
    Optional<List<Message>> messagesOptional;
    boolean initialIndex = false;

    if (groupEntity.getBottomPointer() == null) {
      messagesOptional = groupMeInterface.getMessageBatch(groupId);
      initialIndex = true;
    } else {
      messagesOptional = groupMeInterface.getMessageBatch(groupId, BEFORE_ID, groupEntity.getBottomPointer());
    }
    if (messagesOptional.isEmpty()) {
      LOG.info("No messages found for Group ID " + groupId);
      return;
    }

    List<Message> messages = messagesOptional.get();
    if (initialIndex) {
      groupEntity.setTopPointer(messages.get(0).getId());
      groupRepository.save(groupEntity);
    }

    while (!messages.isEmpty()) {
      String lastMessage = persistMessagesDownward(messages, groupEntity);
      messagesOptional = groupMeInterface.getMessageBatch(groupId, BEFORE_ID, lastMessage);
      if (messagesOptional.isEmpty()) {
        break;
      }
      messages = messagesOptional.get();
    }
    groupEntity.setInitialized(true);
    groupEntity.setBottomPointer(null);
    groupRepository.save(groupEntity);
  }

  private String persistMessagesDownward(List<Message> messages, GroupEntity groupEntity) {
    String lastMessageId = messages.get(messages.size() - 1).getId();
    BulkMessagePersist bulkMessagePersist = new BulkMessagePersist(esMessageIndex.getIndex());
    messages.forEach(bulkMessagePersist::addMessage);
    numMessagesPersisted += bulkMessagePersist.getNumMessages();
    esMessageIndex.executeBulkPersist(bulkMessagePersist);
    groupEntity.setBottomPointer(lastMessageId);
    groupRepository.save(groupEntity);
    return lastMessageId;
  }

  private void indexUpward(GroupEntity groupEntity) {
    // TODO implement me
  }

  /**
   * Returns the number of messages persisted in the most recent run of the update algorithm.
   */
  @VisibleForTesting
  int getNumMessagesPersisted() {
    return numMessagesPersisted;
  }
}
