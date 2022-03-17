package com.search.indexing;

import com.search.configuration.IndexingConfiguration;
import com.search.elasticsearch.EsMessageIndex;
import com.search.groupme.GroupMeMessageDataSource;
import com.search.groupme.MessageDataSource;
import com.search.rdbms.hibernate.repositories.GroupRepository;
import com.search.rdbms.hibernate.repositories.UserRepository;

public class MessageIndexingRunnableImpl extends MessageIndexingRunnable {
  public MessageIndexingRunnableImpl(IndexingConfiguration indexingConfiguration,
                                     UserRepository userRepository,
                                     GroupRepository groupRepository,
                                     EsMessageIndex esMessageIndex) {
    super(indexingConfiguration, userRepository, groupRepository, esMessageIndex);
  }

  @Override
  protected MessageIndexer getMessageIndexer(String accessToken) {
    MessageDataSource dataSource = new GroupMeMessageDataSource(indexingConfiguration.getGroupMeApi(), accessToken);
    return new MessageIndexer(dataSource, esMessageIndex, groupRepository);
  }
}
