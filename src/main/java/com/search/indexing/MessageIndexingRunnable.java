package com.search.indexing;

import com.search.elasticsearch.EsMessageIndex;
import com.search.rdbms.hibernate.models.UserEntity;
import com.search.rdbms.hibernate.repositories.GroupRepository;
import com.search.rdbms.hibernate.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import static com.search.configuration.ConfigConstants.GROUP_ME_API_KEY;
import static com.search.configuration.ConfigConstants.RUN_INDEXING_KEY;

public abstract class MessageIndexingRunnable implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(MessageIndexingRunnable.class);

  protected final GroupRepository groupRepository;
  protected final EsMessageIndex esMessageIndex;
  protected final String groupMeApiEndpoint;

  private final Environment env;
  private final UserRepository userRepository;

  private boolean usersExist;

  public MessageIndexingRunnable(Environment env, UserRepository userRepository, GroupRepository groupRepository,
                                 EsMessageIndex esMessageIndex) {
    this.env = env;
    this.groupMeApiEndpoint = env.getProperty(GROUP_ME_API_KEY);
    this.userRepository = userRepository;
    this.groupRepository = groupRepository;
    this.esMessageIndex = esMessageIndex;
    usersExist = false;
  }

  @Override
  public void run() {
    LOG.info("Running message indexing thread with GroupMe API host: {}.", groupMeApiEndpoint);
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
    String runIndexing = env.getProperty(RUN_INDEXING_KEY);
    if (indexingTurnedOff(runIndexing)) {
      LOG.debug("Message indexing turned off -- skipping iteration.");
      return;
    }
    for (UserEntity user : userRepository.findAll()) {
      usersExist = true;
      updateGroupsForUser(user.getToken());
    }
  }

  private boolean indexingTurnedOff(String runIndexing) {
    return runIndexing != null && runIndexing.equals("false");
  }

  private void updateGroupsForUser(String accessToken) {
    getMessageIndexer(accessToken).updateGroups();
  }

  protected abstract MessageIndexer getMessageIndexer(String accessToken);
}
