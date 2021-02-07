package com.search.groupme;

import com.search.jsonModels.Message;
import com.search.jsonModels.Group;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GroupMeInterfaceTest {

  private GroupMeInterface groupMeInterface;

  @BeforeEach
  public void beforeEach() {
    groupMeInterface = new GroupMeInterface("test");
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

}
