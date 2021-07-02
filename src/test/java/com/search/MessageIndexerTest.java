package com.search;

import com.search.elasticsearch.EsMessageIndex;
import com.search.groupme.GroupMeInterface;
import com.search.jsonModels.Group;
import com.search.jsonModels.Message;
import com.search.rdbms.hibernate.models.GroupEntity;
import com.search.rdbms.hibernate.repositories.GroupRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.search.groupme.GroupMeInterface.MessageQueryType.BEFORE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
class MessageIndexerTest {

  private static final long GROUP_1 = 1;
  private static final long GROUP_2 = 2;

  private GroupMeInterface groupMeInterface;
  private EsMessageIndex esMessageIndex;
  private MessageIndexer messageIndexer;

  @Autowired
  GroupRepository groupRepository;

  @BeforeEach
  public void beforeEach() {
    groupMeInterface = mock(GroupMeInterface.class);
    esMessageIndex = mock(EsMessageIndex.class);
    messageIndexer = new MessageIndexer(groupMeInterface, esMessageIndex, groupRepository);
    setupGroupMeInterfaceMocks();
  }

  @AfterEach
  public void afterEach() {
    groupRepository.deleteAll();
  }

  private void setupGroupMeInterfaceMocks() {
    List<Group> groups = new ArrayList<>();
    groups.add(new Group(GROUP_1, "group1"));
    groups.add(new Group(GROUP_2, "group2"));
    when(groupMeInterface.getAllGroups()).thenReturn(groups);

    List<Message> defaultMessages1 = new ArrayList<>();
    defaultMessages1.add(new Message(1, GROUP_1, "p1", "msg1"));
    defaultMessages1.add(new Message(2, GROUP_1, "p2", "msg2"));
    defaultMessages1.add(new Message(3, GROUP_1, "p3", "msg3"));
    when(groupMeInterface.getMessageBatch(GROUP_1)).thenReturn(Optional.of(defaultMessages1));

    List<Message> before3Group1 = new ArrayList<>();
    before3Group1.add(new Message(4, GROUP_1, "p1", "msg4"));
    before3Group1.add(new Message(5, GROUP_1, "p2", "msg5"));
    before3Group1.add(new Message(6, GROUP_1, "p3", "msg6"));
    when(groupMeInterface.getMessageBatch(GROUP_1, BEFORE_ID, 3L)).thenReturn(Optional.of(before3Group1));

    List<Message> before6Group1 = new ArrayList<>();
    before6Group1.add(new Message(7, GROUP_1, "p1", "msg7"));
    before6Group1.add(new Message(8, GROUP_1, "p2", "msg8"));
    before6Group1.add(new Message(9, GROUP_1, "p3", "msg9"));
    when(groupMeInterface.getMessageBatch(GROUP_1, BEFORE_ID, 6L)).thenReturn(Optional.of(before6Group1));

    List<Message> before9Group1 = new ArrayList<>();
    when(groupMeInterface.getMessageBatch(GROUP_1, BEFORE_ID, 9L)).thenReturn(Optional.of(before9Group1));

    List<Message> defaultMessages2 = new ArrayList<>();
    defaultMessages2.add(new Message(1, GROUP_2, "p1", "msg1"));
    defaultMessages2.add(new Message(2, GROUP_2, "p2", "msg2"));
    defaultMessages2.add(new Message(3, GROUP_2, "p3", "msg3"));
    when(groupMeInterface.getMessageBatch(GROUP_2)).thenReturn(Optional.of(defaultMessages2));

    List<Message> before3Group2 = new ArrayList<>();
    before3Group2.add(new Message(4, GROUP_2, "p1", "msg4"));
    when(groupMeInterface.getMessageBatch(GROUP_2, BEFORE_ID, 3L)).thenReturn(Optional.of(before3Group2));

    List<Message> before4Group2 = new ArrayList<>();
    when(groupMeInterface.getMessageBatch(GROUP_2, BEFORE_ID, 4L)).thenReturn(Optional.of(before4Group2));
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
    groupRepository.save(new GroupEntity(GROUP_1, 1L, null, false));
    executeGroupInitialization();
  }

  private void executeGroupInitialization() {
    messageIndexer.updateGroups();

    assertGroupEntitiesInitialized(GROUP_1);
    assertGroupEntitiesInitialized(GROUP_2);
    assertMessagesPersisted(13, 5);
  }

  @Test
  public void test_GroupUninitialized_HaveBottomPointer() {
    groupRepository.save(new GroupEntity(GROUP_1, 1L, 3L, false));

    messageIndexer.updateGroups();

    assertGroupEntitiesInitialized(GROUP_1);
    assertGroupEntitiesInitialized(GROUP_2);
    assertMessagesPersisted(10, 4);
  }

  @Test
  public void test_GroupUninitialized_BottomPointer_LastMessage() {
    groupRepository.save(new GroupEntity(GROUP_1, 1L, 9L, false));
    groupRepository.save(new GroupEntity(GROUP_2, 1L, 4L, false));

    messageIndexer.updateGroups();

    assertGroupEntitiesInitialized(GROUP_1);
    assertGroupEntitiesInitialized(GROUP_2);
    verify(esMessageIndex, times(0)).executeBulkPersist(any());
  }

  private void assertGroupEntitiesInitialized(long groupId) {
    Optional<GroupEntity> group = groupRepository.findById(groupId);
    assertTrue(group.isPresent());
    assertEquals(new GroupEntity(groupId, 1L, null, true), group.get());
  }

  private void assertMessagesPersisted(int expectedMessagesPersisted, int expectedBulkPersists) {
    assertEquals(expectedMessagesPersisted, messageIndexer.getNumMessagesPersisted());
    verify(esMessageIndex, times(expectedBulkPersists)).executeBulkPersist(any());
  }
}
