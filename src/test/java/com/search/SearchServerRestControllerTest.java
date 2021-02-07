package com.search;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.search.rdbms.hibernate.models.UserEntity;
import com.search.rdbms.hibernate.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class SearchServerRestControllerTest {

  private static final String USERNAME = "user123";
  private static final String TOKEN1 = "token123";
  private static final String TOKEN2 = "token456";

  @Autowired
  MockMvc mvc;

  @Autowired
  UserRepository userRepository;

  @AfterEach
  public void afterEach() {
    userRepository.deleteAll();
  }

  @Test
  public void testGetRoot() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string(equalTo("Greetings from Spring Boot!")));

  }

  @Test
  public void testNewUser() throws Exception {
    // when we hit /newUser with new use details
    mvc.perform(MockMvcRequestBuilders.get("/newUser")
        .param("username", USERNAME)
        .param("access_token", TOKEN1)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());

    // Then a new user has been persisted to the database correctly
    Optional<UserEntity> persistedOptional = userRepository.findById(USERNAME);
    assertTrue(persistedOptional.isPresent());
    UserEntity user = persistedOptional.get();
    assertEquals(user.getUsername(), USERNAME);
    assertEquals(user.getToken(), TOKEN1);

    // And when we hit /newUser with the same user but a different token
    mvc.perform(MockMvcRequestBuilders.get("/newUser")
        .param("username", USERNAME)
        .param("access_token", TOKEN2)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());

    // Then that user gets updated in the database
    persistedOptional = userRepository.findById(USERNAME);
    assertTrue(persistedOptional.isPresent());
    UserEntity user2 = persistedOptional.get();
    assertEquals(user2.getUsername(), USERNAME);
    assertEquals(user2.getToken(), TOKEN2);
  }

}
