package com.search.groupme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.jsonModels.Message;
import com.search.jsonModels.Group;
import com.search.jsonModels.wrappers.GroupResponseWrapper;
import com.search.jsonModels.wrappers.MessageResponseWrapper;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriTemplate;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static com.search.groupme.ApiConstants.*;

public class GroupMeMessageDataSource implements MessageDataSource {

  private static final Logger LOG = LoggerFactory.getLogger(GroupMeMessageDataSource.class);

  private final String accessToken;
  private final GroupmeRequestMaker requestMaker;

  public GroupMeMessageDataSource(String accessToken) {
    this(accessToken, new GroupmeRequestMaker());
  }

  public GroupMeMessageDataSource(String accessToken, GroupmeRequestMaker requestMaker) {
    this.accessToken = accessToken;
    this.requestMaker = requestMaker;
  }

  public Optional<List<Message>> getMessageBatch(String groupId) {
    return getMessageBatch(groupId, null, null);
  }

  public Optional<List<Message>> getMessageBatch(String groupId, MessageQueryType type, String messageId) {
    LOG.debug("Getting message batch from group {} with query type {} from message ID {}",
        groupId, type, messageId);
    try {
      URIBuilder uriBuilder = new URIBuilder(getUriForMessages(groupId));
      uriBuilder.addParameter(TOKEN, accessToken);
      uriBuilder.addParameter(LIMIT, Integer.toString(MSG_LIMIT));

      if (type != null && messageId != null) {
        addQueryParam(type, messageId, uriBuilder);
      }

      URL url = new URL(uriBuilder.build().toString());
      LOG.debug("Making request to: {}", url);
      InputStream responseStream = requestMaker.makeRequest(url);

      ObjectMapper mapper = new ObjectMapper();
      MessageResponseWrapper messageResponseWrapper = mapper.readValue(responseStream, MessageResponseWrapper.class);

      List<Message> messages = messageResponseWrapper.getResponse().getMessages();
      LOG.debug("Received {} messages", messages.size());
      if (messages.size() > 0) {
        LOG.debug("First message: [{}]", messages.get(0));
        LOG.debug("Last message: [{}]", messages.get(messages.size() - 1));
      }
      return Optional.of(messages);
    } catch (Exception e) {
      LOG.error("Error getting messages", e);
    }
    return Optional.empty();
  }

  private URI getUriForMessages(String groupId) {
    UriTemplate uriTemplate = new UriTemplate(URL + "/groups/{group_id}/messages");
    Map<String, String> uriVariables = new HashMap<>();
    uriVariables.put(GROUP_ID, groupId);
    return uriTemplate.expand(uriVariables);
  }

  private void addQueryParam(MessageQueryType type, String messageId, URIBuilder uriBuilder) {
    String param;
    switch (type) {
      case BEFORE_ID:
        param = BEFORE_ID;
        break;
      case SINCE_ID:
        param = SINCE_ID;
        break;
      case AFTER_ID:
        param = AFTER_ID;
        break;
      default:
        throw new IllegalArgumentException("MessageType not recognized: " + type.name() + ", " + type);
    }
    uriBuilder.addParameter(param, messageId);
  }

  public List<Group> getAllGroups() {
    LOG.debug("Getting all groups");
    int i = 1;
    List<Group> groups = new ArrayList<>();
    while (true) {
      Optional<List<Group>> groupsForPage = getGroupsInPage(i);
      if (groupsForPage.isPresent() && groupsForPage.get().size() > 0) {
        groups.addAll(groupsForPage.get());
      } else {
        break;
      }
      i++;
    }
    return groups;
  }

  private Optional<List<Group>> getGroupsInPage(int page) {
    try {
      URIBuilder uriBuilder = new URIBuilder(URL + "/groups");
      uriBuilder.addParameter(TOKEN, accessToken);
      uriBuilder.addParameter(OMIT, "memberships");
      uriBuilder.addParameter(PAGE, Integer.toString(page));
      uriBuilder.addParameter(PER_PAGE, Integer.toString(PAGE_SIZE));
      URL url = new URL(uriBuilder.build().toString());
      LOG.debug("Making request to: {}", url);
      InputStream responseStream = requestMaker.makeRequest(url);
      ObjectMapper mapper = new ObjectMapper();

      GroupResponseWrapper groupResponseWrapper = mapper.readValue(responseStream, GroupResponseWrapper.class);
      List<Group> groups = groupResponseWrapper.getResponse();
      LOG.debug("Received {} groups", groups.size());
      if (groups.size() > 0) {
        LOG.debug("First group: [{}]", groups.get(0));
        LOG.debug("Last group: [{}]", groups.get(groups.size() - 1));
      }
      return Optional.of(groupResponseWrapper.getResponse());
    } catch (Exception e) {
      LOG.error("Error getting groups", e);
    }
    return Optional.empty();
  }
}
