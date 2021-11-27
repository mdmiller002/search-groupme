package com.search.indexing;

import com.search.elasticsearch.EsMessageIndex;
import com.search.rdbms.hibernate.repositories.GroupRepository;
import com.search.rdbms.hibernate.repositories.UserRepository;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.mock;

public class TestMessageIndexingRunnableImpl extends MessageIndexingRunnable {
  private final MessageIndexer messageIndexer;

  public TestMessageIndexingRunnableImpl(Environment env, UserRepository userRepository,
                                         GroupRepository groupRepository, EsMessageIndex esMessageIndex) {
    super(env, userRepository, groupRepository, esMessageIndex);
    messageIndexer = mock(MessageIndexer.class);
  }

  @Override
  protected MessageIndexer getMessageIndexer(String accessToken) {
    return messageIndexer;
  }
}
