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

public class GroupMeInterface {

  private static final Logger LOG = LoggerFactory.getLogger(GroupMeInterface.class);

  private final String accessToken;
  private final GroupmeRequestMaker requestMaker;

  public GroupMeInterface(String accessToken) {
    this(accessToken, new GroupmeRequestMaker());
  }

  public GroupMeInterface(String accessToken, GroupmeRequestMaker requestMaker) {
    this.accessToken = accessToken;
    this.requestMaker = requestMaker;
  }

  /**
   * Get a message batch of 100 messages, with no special query types. Just returns
   * the first 100 messages in the group in descending order.
   * @param groupId The group to get the messages from.
   * @return An optional list of messages received from the batch call.
   */
  public Optional<List<Message>> getMessageBatch(long groupId) {
    return getMessageBatch(groupId, null, null);
  }

  /**
   * Get a message batch of 100 messages, using the query type specified
   * @param groupId The group to get the messages from
   * @param type The message query type, corresponds to GroupMe API's query parameters
   * @param messageId The message ID to get messages before/after/since for
   * @return An optional list of messages received from the batch call
   */
  public Optional<List<Message>> getMessageBatch(long groupId, MessageQueryType type, Long messageId) {
    try {
      URIBuilder uriBuilder = new URIBuilder(getUriForMessages(groupId));
      uriBuilder.addParameter(TOKEN, accessToken);
      uriBuilder.addParameter(LIMIT, Integer.toString(MSG_LIMIT));

      if (type != null && messageId != null) {
        addQueryParam(type, messageId, uriBuilder);
      }

      URL url = new URL(uriBuilder.build().toString());
      InputStream responseStream = requestMaker.makeRequest(url);

      ObjectMapper mapper = new ObjectMapper();
      MessageResponseWrapper messageResponseWrapper = mapper.readValue(responseStream, MessageResponseWrapper.class);

      return Optional.of(messageResponseWrapper.getResponse().getMessages());
    } catch (Exception e) {
      LOG.error("Error getting messages", e);
    }
    return Optional.empty();
  }

  private URI getUriForMessages(long groupId) {
    UriTemplate uriTemplate = new UriTemplate(URL + "/groups/{group_id}/messages");
    Map<String, String> uriVariables = new HashMap<>();
    uriVariables.put(GROUP_ID, String.valueOf(groupId));
    return uriTemplate.expand(uriVariables);
  }

  private void addQueryParam(MessageQueryType type, Long messageId, URIBuilder uriBuilder) {
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
    uriBuilder.addParameter(param, Long.toString(messageId));
  }

  /**
   * Get all groups that the user this interface corresponds to belongs to.
   * @return A list of all groups. List may be empty.
   */
  public List<Group> getAllGroups() {
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
      InputStream responseStream = requestMaker.makeRequest(url);
      ObjectMapper mapper = new ObjectMapper();

      GroupResponseWrapper groupResponseWrapper = mapper.readValue(responseStream, GroupResponseWrapper.class);
      return Optional.of(groupResponseWrapper.getResponse());
    } catch (Exception e) {
      LOG.error("Error getting groups", e);
    }
    return Optional.empty();
  }


  /**
   * The three query parameters the GroupMe API accepts when querying for messages.
   * More details: https://dev.groupme.com/docs/v3#messages
   *
   * Example:
   * Say we have these message IDs (with 1 being most recent, 5 being the least recent):
   * 1, 2, 3, 4, 5
   *
   * before_id=3 returns 4, 5
   * since_id=3 returns 1, 2
   * after_id=3 returns 2, 1
   */
  public enum MessageQueryType {
    BEFORE_ID, // Get messages earlier in time than the ID, descending order
    SINCE_ID,  // Get messages later in time than the ID, descending order
    AFTER_ID   // Get messages later in time than the ID, ascending order
  }

}
