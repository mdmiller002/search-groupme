package com.search.elasticsearch;

import org.elasticsearch.action.support.WriteRequest;
import org.javatuples.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EsMessageIndexTest {

  private final String index = TestIndexHelper.TEST_INDEX;
  private final Message testMessage = new Message("matt", "some text");

  private ClientProvider clientProvider;
  private EsMessageIndex esMessageIndex;

  @BeforeEach
  public void beforeEach() {
    clientProvider = new TestClientManager();
    esMessageIndex = new EsMessageIndex(clientProvider, index);
    // Force index requests to wait until refresh so all tests are deterministic
    esMessageIndex.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
    TestIndexHelper.deleteIndex(clientProvider.get(), index);
  }

  @AfterEach
  public void afterEach() {
    TestIndexHelper.deleteIndex(clientProvider.get(), index);
    clientProvider.close();
  }

  @Test
  public void testMessageToJson() {
    Optional<String> optional = esMessageIndex.messageToJson(testMessage);
    assertTrue(optional.isPresent());
    assertEquals(optional.get(), "{\"sender\":\"matt\",\"text\":\"some text\"}");
  }

  @Test
  public void testMessageToJson_NullMessage() {
    Optional<String> optional = esMessageIndex.messageToJson(null);
    assertTrue(optional.isEmpty());
  }

  @Test
  public void testPersistMessage() {
    esMessageIndex.persistMessage(testMessage);
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>("sender", "matt"));
    termsList.add(new Pair<>("text", "some text"));
    assertTrue(TestIndexHelper.documentExistsInIndex(clientProvider.get(), index, termsList));
  }

  @Test
  public void testPersistMessage_NullMessageNoException() {
    esMessageIndex.persistMessage(null);
    assertFalse(TestIndexHelper.indexExists(clientProvider.get(), index));
  }

  @Test
  public void testPersistMessage_OnlySender() {
    Message message = new Message("just_sender", null);
    esMessageIndex.persistMessage(message);
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>("sender", "just_sender"));
    assertTrue(TestIndexHelper.documentExistsInIndex(clientProvider.get(), index, termsList));
  }

  @Test
  public void testPersistMessage_OnlyMessage() {
    Message message = new Message(null, "just_text");
    esMessageIndex.persistMessage(message);
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>("text", "just_text"));
    assertTrue(TestIndexHelper.documentExistsInIndex(clientProvider.get(), index, termsList));
  }

}
