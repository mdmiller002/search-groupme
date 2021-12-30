package com.search;

import com.search.elasticsearch.EsMessageIndex;
import com.search.jsonModels.Message;
import com.search.rdbms.hibernate.models.UserEntity;
import com.search.rdbms.hibernate.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SearchServerRestControllerUnitTest {

  private static final String USER1 = "user1";
  private static final String USER2 = "user2";
  private static final String TOKEN1 = "token123";
  private static final String TOKEN2 = "token456";
  private static final String GROUP1 = "group1";

  private static final String USERNAME_KEY = "username";
  private static final String ACCESS_TOKEN_KEY = "accessToken";
  private static final String GROUP_ID_KEY = "groupId";
  private static final String NAME_KEY = "name";
  private static final String TEXT_KEY = "text";

  private static final String NEW_USER_API = "/newUser";
  private static final String SEARCH_API = "/search";

  @Autowired private MockMvc mvc;
  @Autowired private UserRepository userRepository;

  @MockBean private EsMessageIndex esMessageIndex;

  @BeforeEach
  public void beforeEach() throws Exception {
    mvc.perform(post(NEW_USER_API)
            .param(USERNAME_KEY, USER1)
            .param(ACCESS_TOKEN_KEY, TOKEN1)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @AfterEach
  public void afterEach() {
    userRepository.deleteAll();
  }

  @Test
  public void testGetRoot() throws Exception {
    mvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(equalTo("GroupMe Search Service is up and available.\n")));

  }

  @Test
  public void testNewUser() throws Exception {
    // when we hit /newUser with new use details
    mvc.perform(post(NEW_USER_API)
        .param(USERNAME_KEY, USER2)
        .param(ACCESS_TOKEN_KEY, TOKEN1)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());

    // Then a new user has been persisted to the database correctly
    Optional<UserEntity> persistedOptional = userRepository.findById(USER2);
    assertTrue(persistedOptional.isPresent());
    UserEntity user = persistedOptional.get();
    assertEquals(USER2, user.getUsername());
    assertEquals(TOKEN1, user.getToken());

    // And when we hit /newUser with the same user but a different token
    mvc.perform(post(NEW_USER_API)
        .param(USERNAME_KEY, USER2)
        .param(ACCESS_TOKEN_KEY, TOKEN2)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());

    // Then that user gets updated in the database
    persistedOptional = userRepository.findById(USER2);
    assertTrue(persistedOptional.isPresent());
    UserEntity user2 = persistedOptional.get();
    assertEquals(USER2, user2.getUsername());
    assertEquals(TOKEN2, user2.getToken());
  }

  @Test
  public void testSearch() throws Exception {
    Message msg1 = new Message("1", GROUP1, "a", "msg1");
    Message msg2 = new Message("2", GROUP1, "b", "msg2");
    when(esMessageIndex.searchForMessage(any())).thenReturn(Arrays.asList(msg1, msg2));
    when(esMessageIndex.searchForMessage(new Message(null, GROUP1, null, "msg1")))
        .thenReturn(List.of(msg1));
    when(esMessageIndex.searchForMessage(new Message(null, GROUP1, "a", null)))
        .thenReturn(List.of(msg1));
    when(esMessageIndex.searchForMessage(new Message(null, GROUP1, "b", "msg2")))
        .thenReturn(List.of(msg2));

    // Searching for group 1 with no name or text specified will return all
    mvc.perform(get(SEARCH_API)
            .param(USERNAME_KEY, USER1)
            .param(ACCESS_TOKEN_KEY, TOKEN1)
            .param(GROUP_ID_KEY, GROUP1)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"numResults\":2,\"results\":[" +
            "{\"id\":\"1\",\"name\":\"a\",\"text\":\"msg1\",\"group_id\":\"group1\"}," +
            "{\"id\":\"2\",\"name\":\"b\",\"text\":\"msg2\",\"group_id\":\"group1\"}]}"));

    // Searching for group 1 msg1 will retrieve just that message
    mvc.perform(get(SEARCH_API)
            .param(USERNAME_KEY, USER1)
            .param(ACCESS_TOKEN_KEY, TOKEN1)
            .param(GROUP_ID_KEY, GROUP1)
            .param(TEXT_KEY, "msg1")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"numResults\":1,\"results\":[" +
            "{\"id\":\"1\",\"name\":\"a\",\"text\":\"msg1\",\"group_id\":\"group1\"}]}"));

    // Searching for group 1 name a will retrieve just one message
    mvc.perform(get(SEARCH_API)
            .param(USERNAME_KEY, USER1)
            .param(ACCESS_TOKEN_KEY, TOKEN1)
            .param(GROUP_ID_KEY, GROUP1)
            .param(NAME_KEY, "a")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"numResults\":1,\"results\":[" +
            "{\"id\":\"1\",\"name\":\"a\",\"text\":\"msg1\",\"group_id\":\"group1\"}]}"));

    // Searching for group 1 name b msg2 will retrieve just that message
    mvc.perform(get(SEARCH_API)
            .param(USERNAME_KEY, USER1)
            .param(ACCESS_TOKEN_KEY, TOKEN1)
            .param(GROUP_ID_KEY, GROUP1)
            .param(NAME_KEY, "b")
            .param(TEXT_KEY, "msg2")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"numResults\":1,\"results\":[" +
            "{\"id\":\"2\",\"name\":\"b\",\"text\":\"msg2\",\"group_id\":\"group1\"}]}"));
  }
}
