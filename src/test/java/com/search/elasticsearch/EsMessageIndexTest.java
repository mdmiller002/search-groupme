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
  private final Message testMessage = new Message("Matt Miller", "Text in a message");

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
    String expected =
        "{" +
        "\"sender\":\"" + testMessage.getSender() + "\"," +
        "\"text\":\"" + testMessage.getText() +
        "\"}";
    assertEquals(expected, optional.get());
  }

  @Test
  public void testMessageToJson_NullMessage() {
    Optional<String> optional = esMessageIndex.messageToJson(null);
    assertTrue(optional.isEmpty());
  }

  @Test
  public void testJsonToMessage() {
    String json =
      "{" +
      "\"sender\":\"" + testMessage.getSender() + "\"," +
      "\"text\":\"" + testMessage.getText() +
      "\"}";
    Optional<Message> optional = esMessageIndex.jsonToMessage(json);
    assertTrue(optional.isPresent());
    assertEquals(testMessage, optional.get());
  }

  @Test
  public void testJsonToMessage_NullJson() {
    Optional<Message> optional = esMessageIndex.jsonToMessage(null);
    assertTrue(optional.isEmpty());
  }

  @Test
  public void testPersistMessage() {
    esMessageIndex.persistMessage(testMessage);
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>(Message.SENDER_KEY, testMessage.getSender()));
    termsList.add(new Pair<>(Message.TEXT_KEY, testMessage.getText()));
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
    termsList.add(new Pair<>(Message.SENDER_KEY, "just_sender"));
    assertTrue(TestIndexHelper.documentExistsInIndex(clientProvider.get(), index, termsList));
  }

  @Test
  public void testPersistMessage_OnlyText() {
    Message message = new Message(null, "just_text");
    esMessageIndex.persistMessage(message);
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>(Message.TEXT_KEY, "just_text"));
    assertTrue(TestIndexHelper.documentExistsInIndex(clientProvider.get(), index, termsList));
  }

  @Test
  public void testSearchMessage() {
    esMessageIndex.persistMessage(testMessage);
    List<Message> messages = esMessageIndex.searchForMessage(testMessage);
    assertEquals(messages.size(), 1);
    assertTrue(messages.contains(testMessage));
  }

  @Test
  public void testSearchMessage_OnlySender() {
    Message searchMsg = new Message(testMessage.getSender(), null);
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_OnlyText() {
    Message searchMsg = new Message(null, testMessage.getText());
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_PartialText() {
    Message searchMsg = new Message(null, "Text");
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_PartialSender() {
    Message searchMsg = new Message("Matt", null);
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_PartialSenderAndText() {
    Message searchMsg = new Message("Miller", "in a");
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_PartialDifferentCase() {
    Message searchMsg = new Message("MATT", "A MESSAGE");
    executeSearchForTestMessage(searchMsg);
  }

  private void executeSearchForTestMessage(Message searchMsg) {
    esMessageIndex.persistMessage(testMessage);
    List<Message> messages = esMessageIndex.searchForMessage(searchMsg);
    assertEquals(messages.size(), 1);
    assertTrue(messages.contains(testMessage));
  }

}
