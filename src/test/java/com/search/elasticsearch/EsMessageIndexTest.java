package com.search.elasticsearch;

import com.search.jsonModels.Message;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.javatuples.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EsMessageIndexTest {

  private final String index = TestIndexHelper.TEST_INDEX;
  private final Message testMessage = new Message(1, "Matt Miller", "Text in a message");

  private ClientProvider clientProvider;
  private EsMessageIndex esMessageIndex;

  @BeforeEach
  public void beforeEach() {
    clientProvider = new TestClientManager();
    TestIndexHelper.deleteIndex(clientProvider.get(), index);
    esMessageIndex = new EsMessageIndex(clientProvider, index);
    // Force index requests to wait until refresh so all tests are deterministic
    esMessageIndex.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
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
        "\"id\":" + testMessage.getId() + "," +
        "\"name\":\"" + testMessage.getName() + "\"," +
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
      "\"id\":" + testMessage.getId() + "," +
      "\"name\":\"" + testMessage.getName() + "\"," +
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
  public void testIndexCreation_Mapping() throws IOException {
    GetMappingsRequest request = new GetMappingsRequest().indices(index);
    GetMappingsResponse response = clientProvider.get().indices().getMapping(request, RequestOptions.DEFAULT);
    Map<String, MappingMetadata> mappings = response.mappings();
    MappingMetadata metadata = mappings.get(index);
    Map<String, Object> mapping = metadata.getSourceAsMap();

    String expected = "{properties={name={type=text}, id={type=long}, text={type=text}}}";
    assertEquals(expected, mapping.toString());
  }

  @Test
  public void testPersistMessage() {
    esMessageIndex.persistMessage(testMessage);
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>(Message.NAME_KEY, testMessage.getName()));
    termsList.add(new Pair<>(Message.TEXT_KEY, testMessage.getText()));
    assertTrue(TestIndexHelper.documentExistsInIndex(clientProvider.get(), index, termsList));
  }

  @Test
  public void testPersistMessage_NullMessageNoException() {
    try {
      esMessageIndex.persistMessage(null);
    } catch (Exception e) {
      fail("Exception should not be thrown:", e);
    }
  }

  @Test
  public void testPersistMessage_OnlyName() {
    Message message = new Message(1, "just_name", null);
    esMessageIndex.persistMessage(message);
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>(Message.NAME_KEY, "just_name"));
    assertTrue(TestIndexHelper.documentExistsInIndex(clientProvider.get(), index, termsList));
  }

  @Test
  public void testPersistMessage_OnlyText() {
    Message message = new Message(1, null, "just_text");
    esMessageIndex.persistMessage(message);
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>(Message.TEXT_KEY, "just_text"));
    assertTrue(TestIndexHelper.documentExistsInIndex(clientProvider.get(), index, termsList));
  }

  @Test
  public void testSearchMessage() {
    esMessageIndex.persistMessage(testMessage);
    List<Message> messages = esMessageIndex.searchForMessage(testMessage);
    assertEquals(1, messages.size());
    assertTrue(messages.contains(testMessage));
  }

  @Test
  public void testSearchMessage_OnlyName() {
    Message searchMsg = new Message(1, testMessage.getName(), null);
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_OnlyText() {
    Message searchMsg = new Message(1, null, testMessage.getText());
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_PartialText() {
    Message searchMsg = new Message(1, null, "Text");
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_PartialName() {
    Message searchMsg = new Message(1, "Matt", null);
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_PartialNameAndText() {
    Message searchMsg = new Message(1, "Miller", "in a");
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_PartialDifferentCase() {
    Message searchMsg = new Message(1, "MATT", "A MESSAGE");
    executeSearchForTestMessage(searchMsg);
  }

  private void executeSearchForTestMessage(Message searchMsg) {
    esMessageIndex.persistMessage(testMessage);
    List<Message> messages = esMessageIndex.searchForMessage(searchMsg);
    assertEquals(1, messages.size());
    assertTrue(messages.contains(testMessage));
  }

}
