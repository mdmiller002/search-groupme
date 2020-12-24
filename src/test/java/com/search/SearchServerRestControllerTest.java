package com.search;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.search.rdbms.hibernate.models.User;
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
  private static final String TOKEN = "token123";

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
        .param("access_token", TOKEN)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk());

    // Then a new user has been persisted to the database correctly
    Optional<User> persistedOptional = userRepository.findById((long) 1);
    assertTrue(persistedOptional.isPresent());
    User user = persistedOptional.get();
    assertEquals(user.getUsername(), USERNAME);
    assertEquals(user.getToken(), TOKEN);
  }

}