package com.search;

import com.search.elasticsearch.EsClientProvider;
import com.search.rdbms.hibernate.models.UserEntity;
import com.search.rdbms.hibernate.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SearchServerRestController {

  private static final Logger LOG = LoggerFactory.getLogger(SearchServerRestController.class);

  @Autowired
  private EsClientProvider esClientProvider;

  @Autowired
  private UserRepository userRepository;

  @RequestMapping("/")
  public String index() {
    return "Greetings from Spring Boot!";
  }

  @GetMapping(value = "/login")
  @ResponseBody
  public void login(@RequestParam("access_token") String accessToken) {
    System.out.println(accessToken);
  }


  @GetMapping(value = "/newUser")
  @ResponseBody
  public void newUser(@RequestParam("username") String username,
                      @RequestParam("access_token") String accessToken) {
    LOG.debug("Received new user request with username " + username);
    UserEntity user;
    if (userRepository.existsById(username)) {
      LOG.debug("User " + username + " exists, updating access token.");
      user = userRepository.getOne(username);
      user.setToken(accessToken);
    } else {
      LOG.debug("User " + username + " does not exist, creating new user.");
      user = new UserEntity(username, accessToken);
    }
    userRepository.save(user);
  }

}
