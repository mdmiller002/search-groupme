package com.search.elasticsearch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EsIndexTest {

  private final String index = TestIndexHelper.TEST_INDEX;
  private final Message testMessage = new Message("matt", "some text");

  private ClientProvider clientProvider;
  private EsIndex esIndex;

  @BeforeEach
  public void beforeEach() {
    clientProvider = new TestClientManager();
    esIndex = new EsIndex(clientProvider, index);
    TestIndexHelper.deleteIndex(clientProvider.get(), index);
  }

  @AfterEach
  public void afterEach() {
    TestIndexHelper.deleteIndex(clientProvider.get(), index);
    clientProvider.close();
  }

  @Test
  public void testMessageToJson() {
    Optional<String> optional = esIndex.messageToJson(testMessage);
    assertTrue(optional.isPresent());
    assertEquals(optional.get(), "{\"sender\":\"matt\",\"text\":\"some text\"}");
  }

  @Test
  public void testMessageToJson_NullMessage() {
    Optional<String> optional = esIndex.messageToJson(null);
    assertTrue(optional.isEmpty());
  }

  @Test
  public void testPersistMessage() {
    esIndex.persistMessage(testMessage);
  }

}
