package com.search;

import com.search.elasticsearch.RestClientManager;
import com.search.rdbms.hibernate.models.User;
import com.search.rdbms.hibernate.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SearchServerRestController {

  @Autowired
  RestClientManager clientManager;

  @Autowired
  UserRepository userRepository;

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
    User user;
    if (userRepository.existsById(username)) {
      user = userRepository.getOne(username);
      user.setToken(accessToken);
    } else {
      user = new User(username, accessToken);
    }
    userRepository.save(user);
  }

}
