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

  private static final long GROUP_1_ID = 1111111111L;
  private static final long GROUP_2_ID = 2222222222L;
  private static final String MSG_1_GROUP_1_TXT = "Text in message 1 group 1";
  private static final String MSG_2_GROUP_1_TXT = "Text in message 2 group 1";
  private static final String MSG_GROUP_2_TXT = "Text in message 1 group 2";

  private final String index = TestIndexHelper.TEST_INDEX;
  private final Message tstMsg1Group1 = new Message(1111111111111111111L, GROUP_1_ID, "Matt Miller", MSG_1_GROUP_1_TXT);
  private final Message tstMsg2Group1 = new Message(1111111111111111112L, GROUP_1_ID, "John Doe", MSG_2_GROUP_1_TXT);
  private final Message tstMsgGroup2 = new Message(2222222222222222222L, GROUP_2_ID, "Matt Miller", MSG_GROUP_2_TXT);

  private ClientProvider clientProvider;
  private EsMessageIndex esMessageIndex;
  private BulkMessagePersist bulkMessagePersist;

  @BeforeEach
  public void beforeEach() {
    clientProvider = new TestClientManager();
    TestIndexHelper.deleteIndex(clientProvider.get(), index);
    esMessageIndex = new EsMessageIndex(clientProvider, index);
    // Force index requests to wait until refresh so all tests are deterministic
    esMessageIndex.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
    bulkMessagePersist = new BulkMessagePersist(index);
  }

  @AfterEach
  public void afterEach() {
    TestIndexHelper.deleteIndex(clientProvider.get(), index);
    clientProvider.close();
  }

  @Test
  public void testMessageToJson() {
    Optional<String> optional = EsMessageIndex.messageToJson(tstMsg1Group1);
    assertTrue(optional.isPresent());
    String expected =
        "{" +
        "\"id\":" + tstMsg1Group1.getId() + "," +
        "\"groupId\":" + tstMsg1Group1.getGroupId() + "," +
        "\"name\":\"" + tstMsg1Group1.getName() + "\"," +
        "\"text\":\"" + tstMsg1Group1.getText() +
        "\"}";
    assertEquals(expected, optional.get());
  }

  @Test
  public void testMessageToJson_NullMessage() {
    Optional<String> optional = EsMessageIndex.messageToJson(null);
    assertTrue(optional.isEmpty());
  }

  @Test
  public void testJsonToMessage() {
    String json =
      "{" +
      "\"id\":" + tstMsg1Group1.getId() + "," +
      "\"groupId\":" + tstMsg1Group1.getGroupId() + "," +
      "\"name\":\"" + tstMsg1Group1.getName() + "\"," +
      "\"text\":\"" + tstMsg1Group1.getText() +
      "\"}";
    Optional<Message> optional = EsMessageIndex.jsonToMessage(json);
    assertTrue(optional.isPresent());
    assertEquals(tstMsg1Group1, optional.get());
  }

  @Test
  public void testJsonToMessage_NullJson() {
    Optional<Message> optional = EsMessageIndex.jsonToMessage(null);
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
    bulkMessagePersist.addMessage(tstMsg1Group1);
    esMessageIndex.executeBulkPersist(bulkMessagePersist);
    Optional<String> docIdOptional = tstMsg1Group1.getDocId();
    assertTrue(docIdOptional.isPresent());
    String docId = docIdOptional.get();
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>(Message.NAME_KEY, tstMsg1Group1.getName()));
    termsList.add(new Pair<>(Message.TEXT_KEY, tstMsg1Group1.getText()));
    assertEquals(1, TestIndexHelper.docSearchableByTerms(clientProvider.get(), index, termsList));
    assertTrue(TestIndexHelper.docExistsInIndexById(clientProvider.get(), index, docId));

    // Persisting same message should upsert instead of adding a second message
    esMessageIndex.executeBulkPersist(bulkMessagePersist);
    assertEquals(1, TestIndexHelper.docSearchableByTerms(clientProvider.get(), index, termsList));
    assertTrue(TestIndexHelper.docExistsInIndexById(clientProvider.get(), index, docId));
  }

  @Test
  public void testPersistBulk() {
    bulkMessagePersist.addMessage(tstMsg1Group1);
    bulkMessagePersist.addMessage(tstMsg2Group1);

    Optional<String> docIdOptional1 = tstMsg1Group1.getDocId();
    assertTrue(docIdOptional1.isPresent());
    String docId1 = docIdOptional1.get();

    Optional<String> docIdOptional2 = tstMsg1Group1.getDocId();
    assertTrue(docIdOptional2.isPresent());
    String docId2 = docIdOptional1.get();

    List<Pair<String, Object>> termsList1 = new ArrayList<>();
    termsList1.add(new Pair<>(Message.NAME_KEY, tstMsg1Group1.getName()));
    termsList1.add(new Pair<>(Message.TEXT_KEY, tstMsg1Group1.getText()));

    List<Pair<String, Object>> termsList2 = new ArrayList<>();
    termsList2.add(new Pair<>(Message.NAME_KEY, tstMsg2Group1.getName()));
    termsList2.add(new Pair<>(Message.TEXT_KEY, tstMsg2Group1.getText()));

    esMessageIndex.executeBulkPersist(bulkMessagePersist);

    assertEquals(1, TestIndexHelper.docSearchableByTerms(clientProvider.get(), index, termsList1));
    assertTrue(TestIndexHelper.docExistsInIndexById(clientProvider.get(), index, docId1));

    assertEquals(1, TestIndexHelper.docSearchableByTerms(clientProvider.get(), index, termsList2));
    assertTrue(TestIndexHelper.docExistsInIndexById(clientProvider.get(), index, docId2));
  }

  @Test
  public void testPersistMessage_NullMessageNoException() {
    try {
      esMessageIndex.executeBulkPersist(null);
    } catch (Exception e) {
      fail("Exception should not be thrown:", e);
    }
  }

  @Test
  public void testPersistMessage_OnlyName() {
    Message message = new Message(1, GROUP_1_ID, "just_name", null);
    bulkMessagePersist.addMessage(message);
    Optional<String> docIdOptional = message.getDocId();
    assertTrue(docIdOptional.isPresent());
    String docId = docIdOptional.get();
    esMessageIndex.executeBulkPersist(bulkMessagePersist);
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>(Message.NAME_KEY, "just_name"));
    assertEquals(1, TestIndexHelper.docSearchableByTerms(clientProvider.get(), index, termsList));
    assertTrue(TestIndexHelper.docExistsInIndexById(clientProvider.get(), index, docId));
  }

  @Test
  public void testPersistMessage_OnlyText() {
    Message message = new Message(1, GROUP_1_ID, null, "just_text");
    bulkMessagePersist.addMessage(message);
    Optional<String> docIdOptional = message.getDocId();
    assertTrue(docIdOptional.isPresent());
    String docId = docIdOptional.get();
    esMessageIndex.executeBulkPersist(bulkMessagePersist);
    List<Pair<String, Object>> termsList = new ArrayList<>();
    termsList.add(new Pair<>(Message.TEXT_KEY, "just_text"));
    assertEquals(1, TestIndexHelper.docSearchableByTerms(clientProvider.get(), index, termsList));
    assertTrue(TestIndexHelper.docExistsInIndexById(clientProvider.get(), index, docId));
  }

  @Test
  public void testSearchMessage() {
    bulkMessagePersist.addMessage(tstMsg1Group1);
    esMessageIndex.executeBulkPersist(bulkMessagePersist);
    List<Message> messages = esMessageIndex.searchForMessage(tstMsg1Group1);
    assertEquals(1, messages.size());
    assertTrue(messages.contains(tstMsg1Group1));
  }

  @Test
  public void testSearchMessage_DifferentGroups() {
    Message msg1 = new Message(1, GROUP_1_ID, "p1", "msg");
    Message msg2 = new Message(1, GROUP_2_ID, "p1", "msg");

    bulkMessagePersist.addMessage(msg1);
    bulkMessagePersist.addMessage(msg2);
    esMessageIndex.executeBulkPersist(bulkMessagePersist);
    List<Message> messages1 = esMessageIndex.searchForMessage(msg1);
    List<Message> messages2 = esMessageIndex.searchForMessage(msg2);
    assertEquals(1, messages1.size());
    assertTrue(messages1.contains(msg1));
    assertFalse(messages1.contains(msg2));
    assertEquals(1, messages2.size());
    assertTrue(messages2.contains(msg2));
    assertFalse(messages2.contains(msg1));
  }

  @Test
  public void testSearchMessage_OnlyName() {
    Message searchMsg = new Message(1, GROUP_1_ID, tstMsg1Group1.getName(), null);
    executeSearchForTestMessage(searchMsg);
  }

  @Test
  public void testSearchMessage_OnlyText() {
    Message searchMsg = new Message(1, GROUP_1_ID, null, tstMsg1Group1.getText());
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
    bulkMessagePersist.addMessage(tstMsg1Group1);
    bulkMessagePersist.addMessage(tstMsgGroup2);
    esMessageIndex.executeBulkPersist(bulkMessagePersist);
    List<Message> messages = esMessageIndex.searchForMessage(searchMsg);
    assertEquals(1, messages.size());
    assertTrue(messages.contains(tstMsg1Group1));
  }

}
