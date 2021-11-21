package com.search;

import com.search.elasticsearch.EsMessageIndex;
import com.search.groupme.MessageDataSource;
import com.search.jsonModels.Group;
import com.search.rdbms.hibernate.models.GroupEntity;
import com.search.rdbms.hibernate.repositories.GroupRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest
public abstract class MessageIndexerTestBase {

  protected static final String GROUP_1 = "1";
  protected static final String GROUP_2 = "2";

  protected MessageDataSource messageDataSource;
  protected EsMessageIndex esMessageIndex;
  protected MessageIndexer messageIndexer;

  @Autowired
  protected GroupRepository groupRepository;

  @BeforeEach
  public void beforeEach() {
    messageDataSource = mock(MessageDataSource.class);
    esMessageIndex = mock(EsMessageIndex.class);
    messageIndexer = new MessageIndexer(messageDataSource, esMessageIndex, groupRepository);

    List<Group> groups = new ArrayList<>();
    groups.add(new Group(GROUP_1, "group1"));
    groups.add(new Group(GROUP_2, "group2"));
    when(messageDataSource.getAllGroups()).thenReturn(groups);

    setupMessageMocks();
  }

  @AfterEach
  public void afterEach() {
    groupRepository.deleteAll();
  }

  protected abstract void setupMessageMocks();

  protected void assertMessagesPersisted(int expectedMessagesPersisted, int expectedBulkPersists) {
    assertEquals(expectedMessagesPersisted, messageIndexer.getNumMessagesPersisted());
    verify(esMessageIndex, times(expectedBulkPersists)).executeBulkPersist(any());
  }

  protected void assertGroupEntityUpdatedAsExpected(String groupId, String expectedTopPointer) {
    Optional<GroupEntity> group = groupRepository.findById(groupId);
    assertTrue(group.isPresent());
    assertEquals(new GroupEntity(groupId, expectedTopPointer, null, true), group.get());
  }
}
