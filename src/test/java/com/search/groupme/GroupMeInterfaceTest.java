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
  public void testGetGroups() {
    Optional<List<Group>> groups = groupMeInterface.getGroups();
    assertTrue(groups.isPresent());
  }

}