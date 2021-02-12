package com.search.groupme;

import com.search.jsonModels.Group;
import com.search.jsonModels.Message;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupMeInterfaceTest {

  private GroupMeInterface groupMeInterface;
  private static String key;

  @BeforeAll
  public static void beforeAll() throws IOException {
    key = getKey();
  }

  @BeforeEach
  public void beforeEach() {
    groupMeInterface = new GroupMeInterface(key);
  }

  @Test
  public void testGetAllGroups() {
    List<Group> groups = groupMeInterface.getAllGroups();
    assertTrue(groups.size() > 0);
  }

  @Test
  public void testGetMessages_default() {
    Optional<List<Message>> messages = groupMeInterface.getMessageBatch(17246470L);
    assertTrue(messages.isPresent());
  }

  @Test
  public void testGetMessages() {
    Optional<List<Message>> messages = groupMeInterface.getMessageBatch(17246470L, GroupMeInterface.MessageQueryType.SINCE_ID, 161266384695074621L);
    assertTrue(messages.isPresent());
  }

  private static String getKey() throws IOException {
    InputStream stream = GroupMeInterface.class.getClassLoader().getResourceAsStream("keys.properties");
    Properties properties = new Properties();
    properties.load(stream);
    return properties.getProperty("key1");
  }

}
