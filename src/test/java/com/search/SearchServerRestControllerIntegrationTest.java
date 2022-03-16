package com.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.search.elasticsearch.BulkMessagePersist;
import com.search.elasticsearch.EsClientProvider;
import com.search.elasticsearch.EsMessageIndex;
import com.search.jsonModels.Message;
import com.search.jsonModels.api.MessagesResponse;
import com.search.jsonModels.api.UsersResponse;
import com.search.rdbms.hibernate.models.UserEntity;
import com.search.rdbms.hibernate.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.search.IntegrationTestConstants.*;
import static com.search.JsonTestUtils.toJson;
import static com.search.elasticsearch.TestIndexHelper.deleteDataFromIndex;
import static org.elasticsearch.action.support.WriteRequest.RefreshPolicy.WAIT_UNTIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(BasicMockServerTestExtension.class)
public class SearchServerRestControllerIntegrationTest {

  private static final String UNAUTHORIZED_MESSAGE = """
      {"message":"User is not a member of this group"}""";

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private EsMessageIndex esMessageIndex;
  @Autowired private EsClientProvider esClientProvider;

  @BeforeEach
  public void beforeEach() {
    userRepository.deleteAll();
    esMessageIndex.setRefreshPolicy(WAIT_UNTIL);
    deleteDataFromIndex(esClientProvider.get(), esMessageIndex.getIndex());
  }

  @AfterEach
  public void afterEach() {
    userRepository.deleteAll();
    deleteDataFromIndex(esClientProvider.get(), esMessageIndex.getIndex());
  }

  @Test
  public void testGetRoot() {
    String res = restTemplate.getForObject(getUrl(""), String.class);
    assertEquals("{\"message\":\"GroupMe Search Service is up and available.\"}", res);
  }

  @Test
  public void testNewUser() throws JsonProcessingException {
    String res = restTemplate.postForObject(getUsersUrl(TOKEN1), null, String.class);
    assertEquals(toJson(new UsersResponse("Created new user user1")), res);
    Optional<UserEntity> user = userRepository.findById(USER1);
    assertTrue(user.isPresent());
    assertEquals(TOKEN1, user.get().getToken());

    res = restTemplate.postForObject(getUsersUrl(TOKEN2), null, String.class);
    assertEquals(toJson(new UsersResponse("Updated access token for user1")), res);
    user = userRepository.findById(USER1);
    assertTrue(user.isPresent());
    assertEquals(TOKEN2, user.get().getToken());
  }

  @Test
  public void testMessages_unauthorized() {
    String res = restTemplate.getForObject(getMessagesUrl(UNAUTHORIZED_GROUP, "Matt", "hello"), String.class);
    assertEquals(UNAUTHORIZED_MESSAGE, res);
    res = restTemplate.getForObject(getMessagesUrl(GROUP1, "Matt", "hello", UNAUTHORIZED_TOKEN), String.class);
    assertEquals(UNAUTHORIZED_MESSAGE, res);
    res = restTemplate.getForObject(getMessagesUrl(INVALID_GROUP, "Matt", "hello"), String.class);
    assertEquals(UNAUTHORIZED_MESSAGE, res);
  }

  @Test
  public void testMessages() throws JsonProcessingException {
    BulkMessagePersist bulk = new BulkMessagePersist(esMessageIndex.getIndex());
    Message g1_msg1 = new Message("1", GROUP1, "Matt", "Hello, this is message 1");
    Message g1_msg2 = new Message("2", GROUP1, "Matt", "Hello, this is message 2");
    Message g1_msg3 = new Message("3", GROUP1, "Jack", "apple");
    Message g1_msg4 = new Message("4", GROUP1, "Jack", "banana");
    Message g1_msg5 = new Message("5", GROUP1, "Matt", "apple");
    bulk.addMessage(g1_msg1);
    bulk.addMessage(g1_msg2);
    bulk.addMessage(g1_msg3);
    bulk.addMessage(g1_msg4);
    bulk.addMessage(g1_msg5);

    Message g2_msg1 = new Message("1", GROUP2, "Zach", "Waaaaaa");
    Message g2_msg2 = new Message("2", GROUP2, "Reno", "Hello Zach");
    Message g2_msg3 = new Message("3", GROUP2, "Este", "Spiderman");
    bulk.addMessage(g2_msg1);
    bulk.addMessage(g2_msg2);
    bulk.addMessage(g2_msg3);

    esMessageIndex.executeBulkPersist(bulk);

    String res = restTemplate.getForObject(getMessagesUrl(GROUP1, "Matt", "Hello, this is message 1"), String.class);
    assertEquals(toJson(new MessagesResponse(2, List.of(g1_msg1, g1_msg2))), res);
    res = restTemplate.getForObject(getMessagesUrl(GROUP1, null, "apple"), String.class);
    assertEquals(toJson(new MessagesResponse(2, List.of(g1_msg3, g1_msg5))), res);
    res = restTemplate.getForObject(getMessagesUrl(GROUP1, "Matt", null), String.class);
    assertEquals(toJson(new MessagesResponse(3, List.of(g1_msg1, g1_msg2, g1_msg5))), res);
    res = restTemplate.getForObject(getMessagesUrl(GROUP1, "Jack", "banana"), String.class);
    assertEquals(toJson(new MessagesResponse(1, List.of(g1_msg4))), res);

    res = restTemplate.getForObject(getMessagesUrl(GROUP2, null, "a"), String.class);
    assertEquals(toJson(new MessagesResponse(0, Collections.emptyList())), res);
    res = restTemplate.getForObject(getMessagesUrl(GROUP2, null, "waaaaaa"), String.class);
    assertEquals(toJson(new MessagesResponse(1, List.of(g2_msg1))), res);
    res = restTemplate.getForObject(getMessagesUrl(GROUP2, null, "hello zachary"), String.class);
    assertEquals(toJson(new MessagesResponse(1, List.of(g2_msg2))), res);
    res = restTemplate.getForObject(getMessagesUrl(GROUP2, null, "SPIDERMAN"), String.class);
    assertEquals(toJson(new MessagesResponse(1, List.of(g2_msg3))), res);
  }

  private String getUrl(String path) {
    return restTemplate.getRootUri() + path;
  }

  private String getUsersUrl(String accessToken) {
    return getUrl("/users") + "?username=" + USER1 + "&accessToken=" + accessToken;
  }

  private String getMessagesUrl(String groupId, String name, String text) {
    return getMessagesUrl(groupId, name, text, TOKEN1);
  }

  private String getMessagesUrl(String groupId, String name, String text, String token) {
    StringBuilder builder = new StringBuilder();
    builder.append(getUrl("/messages")).append("?username=").append(USER1).append("&accessToken=")
        .append(token).append("&groupId=").append(groupId);
    if (name != null) {
      builder.append("&name=").append(name);
    }
    if (text != null) {
      builder.append("&text=").append(text);
    }
    return builder.toString();
  }
}
