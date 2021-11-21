package com.search;

import com.search.jsonModels.Message;
import com.search.rdbms.hibernate.models.GroupEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.search.groupme.MessageQueryType.AFTER_ID;
import static com.search.groupme.MessageQueryType.BEFORE_ID;
import static org.mockito.Mockito.*;

public class MessageIndexerInitializationTest extends MessageIndexerTestBase {

  @Override
  protected void setupMessageMocks() {
    List<Message> defaultMessages1 = new ArrayList<>();
    defaultMessages1.add(new Message("1", GROUP_1, "p1", "msg1"));
    defaultMessages1.add(new Message("2", GROUP_1, "p2", "msg2"));
    defaultMessages1.add(new Message("3", GROUP_1, "p3", "msg3"));
    when(messageDataSource.getMessageBatch(GROUP_1)).thenReturn(Optional.of(defaultMessages1));

    List<Message> before3Group1 = new ArrayList<>();
    before3Group1.add(new Message("4", GROUP_1, "p1", "msg4"));
    before3Group1.add(new Message("5", GROUP_1, "p2", "msg5"));
    before3Group1.add(new Message("6", GROUP_1, "p3", "msg6"));
    when(messageDataSource.getMessageBatch(GROUP_1, BEFORE_ID, "3")).thenReturn(Optional.of(before3Group1));

    List<Message> before6Group1 = new ArrayList<>();
    before6Group1.add(new Message("7", GROUP_1, "p1", "msg7"));
    before6Group1.add(new Message("8", GROUP_1, "p2", "msg8"));
    before6Group1.add(new Message("9", GROUP_1, "p3", "msg9"));
    when(messageDataSource.getMessageBatch(GROUP_1, BEFORE_ID, "6")).thenReturn(Optional.of(before6Group1));

    List<Message> before9Group1 = new ArrayList<>();
    when(messageDataSource.getMessageBatch(GROUP_1, BEFORE_ID, "9")).thenReturn(Optional.of(before9Group1));

    List<Message> after1Group1 = new ArrayList<>();
    after1Group1.add(new Message("1.1", GROUP_1, "p1", "msg1.1"));
    after1Group1.add(new Message("1.2", GROUP_1, "p2", "msg1.2"));
    after1Group1.add(new Message("1.3", GROUP_1, "p3", "msg1.3"));
    when(messageDataSource.getMessageBatch(GROUP_1, AFTER_ID, "1")).thenReturn(Optional.of(after1Group1));

    List<Message> after13Group1 = new ArrayList<>();
    after13Group1.add(new Message("1.4", GROUP_1, "p1", "msg1.4"));
    after13Group1.add(new Message("1.5", GROUP_1, "p2", "msg1.5"));
    after13Group1.add(new Message("1.6", GROUP_1, "p3", "msg1.6"));
    when(messageDataSource.getMessageBatch(GROUP_1, AFTER_ID, "1.3")).thenReturn(Optional.of(after13Group1));

    List<Message> after16Group1 = new ArrayList<>();
    after16Group1.add(new Message("1.7", GROUP_1, "p1", "msg1.7"));
    after16Group1.add(new Message("1.8", GROUP_1, "p2", "msg1.8"));
    after16Group1.add(new Message("1.9", GROUP_1, "p3", "msg1.9"));
    when(messageDataSource.getMessageBatch(GROUP_1, AFTER_ID, "1.6")).thenReturn(Optional.of(after16Group1));

    List<Message> defaultMessages2 = new ArrayList<>();
    defaultMessages2.add(new Message("1", GROUP_2, "p1", "msg1"));
    defaultMessages2.add(new Message("2", GROUP_2, "p2", "msg2"));
    defaultMessages2.add(new Message("3", GROUP_2, "p3", "msg3"));
    when(messageDataSource.getMessageBatch(GROUP_2)).thenReturn(Optional.of(defaultMessages2));

    List<Message> before3Group2 = new ArrayList<>();
    before3Group2.add(new Message("4", GROUP_2, "p1", "msg4"));
    when(messageDataSource.getMessageBatch(GROUP_2, BEFORE_ID, "3")).thenReturn(Optional.of(before3Group2));

    List<Message> before4Group2 = new ArrayList<>();
    when(messageDataSource.getMessageBatch(GROUP_2, BEFORE_ID, "4")).thenReturn(Optional.of(before4Group2));
  }

  @Test
  public void test_GroupUnInitialized() {
    executeGroupInitialization();
  }

  @Test
  public void test_GroupUnInitialized_GroupEntityExists() {
    groupRepository.save(new GroupEntity(GROUP_1, null, null, false));
    executeGroupInitialization();
  }

  @Test
  public void test_GroupUnInitialized_OnlyTopPointer() {
    groupRepository.save(new GroupEntity(GROUP_1, "1", null, false));
    executeGroupInitialization();
  }

  private void executeGroupInitialization() {
    messageIndexer.updateGroups();

    assertGroupEntityUpdatedAsExpected(GROUP_1, "1");
    assertGroupEntityUpdatedAsExpected(GROUP_2, "1");
    assertMessagesPersisted(13, 5);
  }

  @Test
  public void test_GroupUninitialized_HaveBottomPointer() {
    groupRepository.save(new GroupEntity(GROUP_1, "1", "3", false));

    messageIndexer.updateGroups();

    assertGroupEntityUpdatedAsExpected(GROUP_1, "1");
    assertGroupEntityUpdatedAsExpected(GROUP_2, "1");
    assertMessagesPersisted(10, 4);
  }

  @Test
  public void test_GroupUninitialized_BottomPointer_LastMessage() {
    groupRepository.save(new GroupEntity(GROUP_1, "1", "9", false));
    groupRepository.save(new GroupEntity(GROUP_2, "1", "4", false));

    messageIndexer.updateGroups();

    assertGroupEntityUpdatedAsExpected(GROUP_1, "1");
    assertGroupEntityUpdatedAsExpected(GROUP_2, "1");
    verify(esMessageIndex, times(0)).executeBulkPersist(any());
  }
}
