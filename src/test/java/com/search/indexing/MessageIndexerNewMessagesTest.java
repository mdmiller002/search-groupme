package com.search.indexing;

import com.search.jsonModels.Message;
import com.search.rdbms.hibernate.models.GroupEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.search.groupme.MessageQueryType.AFTER_ID;
import static org.mockito.Mockito.when;

public class MessageIndexerNewMessagesTest extends MessageIndexerTestBase {

  @Override
  protected void setupMessageMocks() {
    List<Message> after0Group1 = new ArrayList<>();
    after0Group1.add(new Message("1", GROUP_1, "p1", "msg1"));
    after0Group1.add(new Message("2", GROUP_1, "p1", "msg2"));
    after0Group1.add(new Message("3", GROUP_1, "p1", "msg3"));
    when(messageDataSource.getMessageBatch(GROUP_1, AFTER_ID, "0")).thenReturn(Optional.of(after0Group1));

    List<Message> after3Group1 = new ArrayList<>();
    after3Group1.add(new Message("4", GROUP_1, "p1", "msg4"));
    after3Group1.add(new Message("5", GROUP_1, "p1", "msg5"));
    after3Group1.add(new Message("6", GROUP_1, "p1", "msg6"));
    when(messageDataSource.getMessageBatch(GROUP_1, AFTER_ID, "3")).thenReturn(Optional.of(after3Group1));

    List<Message> after6Group1 = new ArrayList<>();
    after6Group1.add(new Message("7", GROUP_1, "p1", "msg7"));
    after6Group1.add(new Message("8", GROUP_1, "p1", "msg8"));
    after6Group1.add(new Message("9", GROUP_1, "p1", "msg9"));
    when(messageDataSource.getMessageBatch(GROUP_1, AFTER_ID, "6")).thenReturn(Optional.of(after6Group1));

    when(messageDataSource.getMessageBatch(GROUP_1, AFTER_ID, "9")).thenReturn(Optional.empty());

    List<Message> after0Group2 = new ArrayList<>();
    after0Group2.add(new Message("1", GROUP_2, "p1", "msg1"));
    when(messageDataSource.getMessageBatch(GROUP_2, AFTER_ID, "0")).thenReturn(Optional.of(after0Group2));

    when(messageDataSource.getMessageBatch(GROUP_2, AFTER_ID, "1")).thenReturn(Optional.empty());
  }

  @Test
  public void test_GroupInitialized_NewMessages() {
    groupRepository.save(new GroupEntity(GROUP_1, "0", null, true));
    groupRepository.save(new GroupEntity(GROUP_2, "0", null, true));
    messageIndexer.updateGroups();
    assertGroupEntityUpdatedAsExpected(GROUP_1, "9");
    assertGroupEntityUpdatedAsExpected(GROUP_2, "1");
    assertMessagesPersisted(10, 4);
  }

  @Test
  public void test_GroupInitialized_NoNewMessages() {
    groupRepository.save(new GroupEntity(GROUP_1, "9", null, true));
    groupRepository.save(new GroupEntity(GROUP_2, "1", null, true));
    messageIndexer.updateGroups();
    assertGroupEntityUpdatedAsExpected(GROUP_1, "9");
    assertGroupEntityUpdatedAsExpected(GROUP_2, "1");
    assertMessagesPersisted(0, 0);
  }
}
