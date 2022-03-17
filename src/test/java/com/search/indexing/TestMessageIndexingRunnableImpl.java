package com.search.indexing;

import com.search.configuration.IndexingConfiguration;
import com.search.elasticsearch.EsMessageIndex;
import com.search.rdbms.hibernate.repositories.GroupRepository;
import com.search.rdbms.hibernate.repositories.UserRepository;

import static org.mockito.Mockito.mock;

public class TestMessageIndexingRunnableImpl extends MessageIndexingRunnable {
  private final MessageIndexer messageIndexer;

  public TestMessageIndexingRunnableImpl(IndexingConfiguration indexingConfiguration,
                                         UserRepository userRepository,
                                         GroupRepository groupRepository,
                                         EsMessageIndex esMessageIndex) {
    super(indexingConfiguration, userRepository, groupRepository, esMessageIndex);
    messageIndexer = mock(MessageIndexer.class);
  }

  @Override
  protected MessageIndexer getMessageIndexer(String accessToken) {
    return messageIndexer;
  }
}
