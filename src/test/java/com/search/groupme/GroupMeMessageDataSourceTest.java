package com.search.groupme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.jsonModels.Group;
import com.search.jsonModels.Message;
import com.search.jsonModels.wrappers.GroupResponseWrapper;
import com.search.jsonModels.wrappers.MessageResponseWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupMeMessageDataSourceTest {

  private static final String KEY = "key";
  private static final String GM_API = "https://api.groupme.com/v3/%s?token=" + KEY + "%s";
  private static final String TEST_GROUP = "1";

  private static final String meta = """
      "meta":{"code":200}}""";

  // Groups
  private static final String GROUPS_PG1_URL = String.format(GM_API, "groups", "&omit=memberships&page=1&per_page=100");
  private static final String GROUPS_PG2_URL = String.format(GM_API, "groups", "&omit=memberships&page=2&per_page=100");

  private static final String GROUP_PG1_RESP = """
      {"response":[{"id":"1","name":"Group A"},
      {"id":"2","name":"Group B"}],""" + meta;

  private static final String GROUP_PG2_RESP = """
      {"response":[],""" + meta;

  // Messages
  private static final String MSG_DEFAULT_URL = String.format(GM_API, "groups/" + TEST_GROUP + "/messages", "&limit=100");
  private static final String MSG_DEFAULT_RESP = """
      {"response":{"messages":[
      {"id": "1", "group_id": "1", "name": "p1", "text": "msg1"},
      {"id": "2", "group_id": "1","name": "p2", "text": "msg2"},
      {"id": "3", "group_id": "1","name": "p3", "text": "msg3"},
      {"id": "4", "group_id": "1","name": "p4", "text": "msg4"},
      {"id": "5", "group_id": "1","name": "p5", "text": "msg5"}]},""" + meta;

  private static final String MSG_BEFORE_URL = String.format(GM_API, "groups/" + TEST_GROUP + "/messages",
      "&limit=100&before_id=3");
  private static final String MSG_BEFORE_RESP = """
      {"response":{"messages":[
      {"id": "4", "group_id": "1","name": "p4", "text": "msg4"},
      {"id": "5", "group_id": "1","name": "p5", "text": "msg5"}]},""" + meta;

  private static final String MSG_AFTER_URL = String.format(GM_API, "groups/" + TEST_GROUP + "/messages",
      "&limit=100&after_id=3");
  private static final String MSG_AFTER_RESP = """
      {"response":{"messages":[
      {"id": "2", "group_id": "1","name": "p2", "text": "msg2"},
      {"id": "1", "group_id": "1","name": "p1", "text": "msg1"}]},""" + meta;

  private static final String MSG_SINCE_URL = String.format(GM_API, "groups/" + TEST_GROUP + "/messages",
      "&limit=100&since_id=3");
  private static final String MSG_SINCE_RESP = """
      {"response":{"messages":[
      {"id": "1", "group_id": "1","name": "p1", "text": "msg1"},
      {"id": "2", "group_id": "1","name": "p2", "text": "msg2"}]},""" + meta;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private static GroupmeRequestMaker requestMaker;
  private GroupMeMessageDataSource messageDataSource;

  @BeforeEach
  public void beforeEach() throws IOException {
    setupMockRequestMaker();
    messageDataSource = new GroupMeMessageDataSource(ApiConstants.URL, KEY, requestMaker);
  }

  private void setupMockRequestMaker() throws IOException {
    requestMaker = mock(GroupmeRequestMaker.class);
    when(requestMaker.makeRequest(any())).thenReturn(null);

    // Groups
    when(requestMaker.makeRequest(new URL(GROUPS_PG1_URL))).thenReturn(makeInputStream(GROUP_PG1_RESP));
    when(requestMaker.makeRequest(new URL(GROUPS_PG2_URL))).thenReturn(makeInputStream(GROUP_PG2_RESP));

    // Messages
    when(requestMaker.makeRequest(new URL(MSG_DEFAULT_URL))).thenReturn(makeInputStream(MSG_DEFAULT_RESP));
    when(requestMaker.makeRequest(new URL(MSG_BEFORE_URL))).thenReturn(makeInputStream(MSG_BEFORE_RESP));
    when(requestMaker.makeRequest(new URL(MSG_AFTER_URL))).thenReturn(makeInputStream(MSG_AFTER_RESP));
    when(requestMaker.makeRequest(new URL(MSG_SINCE_URL))).thenReturn(makeInputStream(MSG_SINCE_RESP));
  }

  private InputStream makeInputStream(String s) {
    return new ByteArrayInputStream(s.getBytes());
  }

  @Test
  public void testGetAllGroups() throws IOException {
    List<Group> groups = messageDataSource.getAllGroups();
    List<Group> expected = objectMapper.readValue(GROUP_PG1_RESP, GroupResponseWrapper.class)
        .getResponse();
    assertTrue(groups.size() > 0);
    assertEquals(expected, groups);
  }

  @Test
  public void testGetAllGroups_nullResult() throws IOException {
    when(requestMaker.makeRequest(new URL(GROUPS_PG1_URL))).thenReturn(null);
    List<Group> groups = messageDataSource.getAllGroups();
    assertEquals(0, groups.size());
  }

  @Test
  public void testGetMessages_default() throws JsonProcessingException {
    Optional<List<Message>> messages = messageDataSource.getMessageBatch(TEST_GROUP);
    assertTrue(messages.isPresent());
    List<Message> expected = objectMapper.readValue(MSG_DEFAULT_RESP, MessageResponseWrapper.class)
        .getResponse().getMessages();
    assertEquals(expected, messages.get());
  }

  @Test
  public void testGetMessage_beforeId() throws JsonProcessingException {
    doTestMessageQuery(MessageQueryType.BEFORE_ID, MSG_BEFORE_RESP);
  }

  @Test
  public void testGetMessage_afterId() throws JsonProcessingException {
    doTestMessageQuery(MessageQueryType.AFTER_ID, MSG_AFTER_RESP);
  }

  @Test
  public void testGetMessage_sinceId() throws JsonProcessingException {
    doTestMessageQuery(MessageQueryType.SINCE_ID, MSG_SINCE_RESP);
  }

  private void doTestMessageQuery(MessageQueryType type, String expectedRespStr)
      throws JsonProcessingException {
    Optional<List<Message>> messages = messageDataSource.getMessageBatch(TEST_GROUP, type, "3");
    assertTrue(messages.isPresent());
    List<Message> expected = objectMapper.readValue(expectedRespStr, MessageResponseWrapper.class)
        .getResponse().getMessages();
    assertEquals(expected, messages.get());
  }

  @Test
  public void testGetMessages_nullResult() throws IOException {
    when(requestMaker.makeRequest(new URL(MSG_DEFAULT_URL))).thenReturn(null);
    Optional<List<Message>> messages = messageDataSource.getMessageBatch(TEST_GROUP);
    assertTrue(messages.isEmpty());
  }

}
