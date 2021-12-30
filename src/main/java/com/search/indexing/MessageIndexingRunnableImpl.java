package com.search.indexing;

import com.search.elasticsearch.EsMessageIndex;
import com.search.groupme.GroupMeMessageDataSource;
import com.search.groupme.MessageDataSource;
import com.search.rdbms.hibernate.repositories.GroupRepository;
import com.search.rdbms.hibernate.repositories.UserRepository;
import org.springframework.core.env.Environment;

public class MessageIndexingRunnableImpl extends MessageIndexingRunnable {
  public MessageIndexingRunnableImpl(Environment env, UserRepository userRepository,
                                     GroupRepository groupRepository, EsMessageIndex esMessageIndex) {
    super(env, userRepository, groupRepository, esMessageIndex);
  }

  @Override
  protected MessageIndexer getMessageIndexer(String accessToken) {
    MessageDataSource dataSource = new GroupMeMessageDataSource(accessToken);
    return new MessageIndexer(dataSource, esMessageIndex, groupRepository);
  }
}