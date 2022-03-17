package com.search.indexing;

import com.search.configuration.IndexingConfiguration;
import com.search.elasticsearch.EsMessageIndex;
import com.search.rdbms.hibernate.models.UserEntity;
import com.search.rdbms.hibernate.repositories.GroupRepository;
import com.search.rdbms.hibernate.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageIndexingRunnable implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(MessageIndexingRunnable.class);

  protected final GroupRepository groupRepository;
  protected final EsMessageIndex esMessageIndex;
  protected final IndexingConfiguration indexingConfiguration;

  private final UserRepository userRepository;

  private boolean usersExist;

  public MessageIndexingRunnable(IndexingConfiguration indexingConfiguration,
                                 UserRepository userRepository,
                                 GroupRepository groupRepository,
                                 EsMessageIndex esMessageIndex) {
    this.indexingConfiguration = indexingConfiguration;
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.esMessageIndex = esMessageIndex;
    usersExist = false;
  }

  @Override
  public void run() {
    LOG.info("Running message indexing thread with GroupMe API host: {}.", indexingConfiguration.getGroupMeApi());
    while (true) {
      runIndexingIteration();
      if (!usersExist) {
        LOG.debug("No users exist yet, waiting 10 seconds.");
        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          LOG.error("Unable to sleep in indexing thread [{}]", Thread.currentThread().getId(), e);
        }
      }
    }
  }

  void runIndexingIteration() {
    if (!indexingConfiguration.runIndexing()) {
      LOG.debug("Message indexing turned off -- skipping iteration.");
      return;
    }
    for (UserEntity user : userRepository.findAll()) {
      usersExist = true;
      updateGroupsForUser(user.getToken());
    }
  }

  private void updateGroupsForUser(String accessToken) {
    getMessageIndexer(accessToken).updateGroups();
  }

  protected abstract MessageIndexer getMessageIndexer(String accessToken);
}
