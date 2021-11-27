package com.search.indexing;

import com.search.elasticsearch.EsMessageIndex;
import com.search.rdbms.hibernate.models.UserEntity;
import com.search.rdbms.hibernate.repositories.GroupRepository;
import com.search.rdbms.hibernate.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.List;

import static com.search.configuration.ConfigConstants.RUN_INDEXING_KEY;
import static org.mockito.Mockito.*;

public class MessageIndexingRunnableTest {

  private Environment environment;
  private UserRepository userRepository;
  private GroupRepository groupRepository;
  private EsMessageIndex esMessageIndex;
  private MessageIndexingRunnable thread;
  private MessageIndexer testMessageIndexer;

  @BeforeEach
  public void beforeEach() {
    environment = mock(Environment.class);
    userRepository = mock(UserRepository.class);
    groupRepository = mock(GroupRepository.class);
    esMessageIndex = mock(EsMessageIndex.class);
    thread = new TestMessageIndexingRunnableImpl(environment, userRepository,
        groupRepository, esMessageIndex);
    testMessageIndexer = thread.getMessageIndexer(null);

    when(environment.getProperty(RUN_INDEXING_KEY)).thenReturn(null);
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
    when(environment.getProperty(RUN_INDEXING_KEY)).thenReturn("false");
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
