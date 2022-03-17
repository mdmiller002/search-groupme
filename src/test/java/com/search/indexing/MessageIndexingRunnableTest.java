package com.search.indexing;

import com.search.configuration.IndexingConfiguration;
import com.search.elasticsearch.EsMessageIndex;
import com.search.rdbms.hibernate.models.UserEntity;
import com.search.rdbms.hibernate.repositories.GroupRepository;
import com.search.rdbms.hibernate.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class MessageIndexingRunnableTest {

  private IndexingConfiguration indexingConfiguration;
  private UserRepository userRepository;
  private MessageIndexingRunnable thread;
  private MessageIndexer testMessageIndexer;

  @BeforeEach
  public void beforeEach() {
    indexingConfiguration = new IndexingConfiguration();
    userRepository = mock(UserRepository.class);
    GroupRepository groupRepository = mock(GroupRepository.class);
    EsMessageIndex esMessageIndex = mock(EsMessageIndex.class);
    thread = new TestMessageIndexingRunnableImpl(indexingConfiguration, userRepository,
        groupRepository, esMessageIndex);
    testMessageIndexer = thread.getMessageIndexer(null);
  }

  @Test
  public void testRun_NoGroups() {
    when(userRepository.findAll()).thenReturn(Collections.emptyList());
    thread.runIndexingIteration();
    verify(testMessageIndexer, never()).updateGroups();
  }

  @Test
  public void testRun_OneUser() {
    setupUserRepoMock(new UserEntity("a", "a"));
    thread.runIndexingIteration();
    verify(testMessageIndexer, times(1)).updateGroups();
  }

  @Test
  public void testRun_MultipleUsers() {
   setupMultipleUsers();
    thread.runIndexingIteration();
    verify(testMessageIndexer, times(3)).updateGroups();
  }

  @Test
  public void testRun_ToggledOff() {
    setupMultipleUsers();
    indexingConfiguration.setRunIndexing(false);
    thread.runIndexingIteration();
    verify(testMessageIndexer, never()).updateGroups();
  }

  private void setupMultipleUsers() {
    setupUserRepoMock(new UserEntity("a", "a"),
        new UserEntity("b", "b"),
        new UserEntity("c", "c"));
  }

  private void setupUserRepoMock(UserEntity... userEntities) {
    when(userRepository.findAll()).thenReturn(List.of(userEntities));
  }
}
