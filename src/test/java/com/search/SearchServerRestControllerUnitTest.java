package com.search;

import com.search.elasticsearch.EsMessageIndex;
import com.search.jsonModels.Message;
import com.search.jsonModels.api.MessagesResponse;
import com.search.jsonModels.api.ServerStatus;
import com.search.jsonModels.api.UsersResponse;
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
import java.util.Collections;
import java.util.List;

import static com.search.JsonTestUtils.toJson;
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
  private static final String TOKEN1 = "token123";
  private static final String GROUP1 = "group1";

  private static final String USERNAME_KEY = "username";
  private static final String ACCESS_TOKEN_KEY = "accessToken";
  private static final String GROUP_ID_KEY = "groupId";
  private static final String NAME_KEY = "name";
  private static final String TEXT_KEY = "text";

  private static final String NEW_USER_API = "/users";
  private static final String SEARCH_API = "/messages";

  @Autowired private MockMvc mvc;
  @Autowired private UserRepository userRepository;

  @MockBean private EsMessageIndex esMessageIndex;

  @BeforeEach
  public void beforeEach() throws Exception {
    userRepository.deleteAll();
    mvc.perform(post(NEW_USER_API)
            .param(USERNAME_KEY, USER1)
            .param(ACCESS_TOKEN_KEY, TOKEN1)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(toJson(new UsersResponse("Created new user user1"))));
  }

  @AfterEach
  public void afterEach() {
    userRepository.deleteAll();
  }

  @Test
  public void testGetRoot() throws Exception {
    mvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(toJson(new ServerStatus("GroupMe Search Service is up and available."))));
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

  @Test
  public void testSearch_noResults() throws Exception {
    when(esMessageIndex.searchForMessage(any())).thenReturn(Collections.emptyList());
    mvc.perform(get(SEARCH_API)
            .param(USERNAME_KEY, USER1)
            .param(ACCESS_TOKEN_KEY, TOKEN1)
            .param(GROUP_ID_KEY, GROUP1)
            .param(NAME_KEY, "a")
            .param(TEXT_KEY, "msg1")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(toJson(new MessagesResponse(0, Collections.emptyList()))));
  }
}
