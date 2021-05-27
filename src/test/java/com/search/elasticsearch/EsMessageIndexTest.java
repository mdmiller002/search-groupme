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

  private static final long GROUP_1_ID = 1;
  private static final long GROUP_2_ID = 2;
  private static final String MSG_GROUP_1_TXT = "Text in message 1";
  private static final String MSG_GROUT_2_TXT = "Text in message 2";

  private final String index = TestIndexHelper.TEST_INDEX;
  private final Message tstMsgGroup1 = new Message(1, GROUP_1_ID, "Matt Miller", MSG_GROUP_1_TXT);
  private final Message tstMsgGroup2 = new Message(1, GROUP_2_ID, "Matt Miller", MSG_GROUT_2_TXT);

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
    Optional<String> optional = esMessageIndex.messageToJson(tstMsgGroup1);
    assertTrue(optional.isPresent());
    String expected =
        "{" +
        "\"id\":" + tstMsgGroup1.getId() + "," +
        "\"groupId\":" + tstMsgGroup1.getGroupId() + "," +
        "\"name\":\"" + tstMsgGroup1.getName() + "\"," +
        "\"text\":\"" + tstMsgGroup1.getText() +
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
      "\"id\":" + tstMsgGroup1.getId() + "," +
      "\"groupId\":" + tstMsgGroup1.getGroupId() + "," +
      "\"name\":\"" + tstMsgGroup1.getName() + "\"," +
      "\"text\":\"" + tstMsgGroup1.getText() +
      "\"}";
    Optional<Message> optional = esMessageIndex.jsonToMessage(json);
    assertTrue(optional.isPresent());
    assertEquals(tstMsgGroup1, optional.get());
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

    String expected = "{properties={groupId={type=long}, name={type=text}, id={type=long}, text={type=text}}}";
    assertEquals(expected, mapping.toString());
  }

  @Test
  public void testPersistMessage() {
    esMessageIndex.persistMessage(tstMsgGroup1);
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>(Message.NAME_KEY, tstMsgGroup1.getName()));
    termsList.add(new Pair<>(Message.TEXT_KEY, tstMsgGroup1.getText()));
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
    Message message = new Message(1, GROUP_1_ID, "just_name", null);
    esMessageIndex.persistMessage(message);
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>(Message.NAME_KEY, "just_name"));
    assertTrue(TestIndexHelper.documentExistsInIndex(clientProvider.get(), index, termsList));
  }

  @Test
  public void testPersistMessage_OnlyText() {
    Message message = new Message(1, GROUP_1_ID, null, "just_text");
    esMessageIndex.persistMessage(message);
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>(Message.TEXT_KEY, "just_text"));
    assertTrue(TestIndexHelper.documentExistsInIndex(clientProvider.get(), index, termsList));
  }

  @Test
  public void testSearchMessage() {
    esMessageIndex.persistMessage(tstMsgGroup1);
    List<Message> messages = esMessageIndex.searchForMessage(tstMsgGroup1);
    assertEquals(1, messages.size());
    assertTrue(messages.contains(tstMsgGroup1));
  }

  @Test
  public void testSearchMessage_DifferentGroups() {
    esMessageIndex.persistMessage(tstMsgGroup1);
    esMessageIndex.persistMessage(tstMsgGroup2);
    List<Message> messages1 = esMessageIndex.searchForMessage(tstMsgGroup1);
    List<Message> messages2 = esMessageIndex.searchForMessage(tstMsgGroup2);
    assertEquals(1, messages1.size());
    assertTrue(messages1.contains(tstMsgGroup1));
    assertFalse(messages1.contains(tstMsgGroup2));
    assertEquals(1, messages2.size());
    assertTrue(messages2.contains(tstMsgGroup2));
    assertFalse(messages2.contains(tstMsgGroup1));
  }

  @Test
  public void testSearchMessage_OnlyName() {
    Message searchMsg = new Message(1, GROUP_1_ID, tstMsgGroup1.getName(), null);
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_OnlyText() {
    Message searchMsg = new Message(1, GROUP_1_ID, null, tstMsgGroup1.getText());
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_PartialText() {
    Message searchMsg = new Message(1, GROUP_1_ID, null, "Text");
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_PartialName() {
    Message searchMsg = new Message(1, GROUP_1_ID, "Matt", null);
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_PartialNameAndText() {
    Message searchMsg = new Message(1, GROUP_1_ID, "Miller", "in a");
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_PartialDifferentCase() {
    Message searchMsg = new Message(1, GROUP_1_ID, "MATT", "A MESSAGE");
    executeSearchForTestMessage(searchMsg);
  }

  private void executeSearchForTestMessage(Message searchMsg) {
    esMessageIndex.persistMessage(tstMsgGroup1);
    esMessageIndex.persistMessage(tstMsgGroup2);
    List<Message> messages = esMessageIndex.searchForMessage(searchMsg);
    assertEquals(1, messages.size());
    assertTrue(messages.contains(tstMsgGroup1));
  }

}
