package com.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.search.elasticsearch.EsMessageIndex;
import com.search.jsonModels.Message;
import com.search.jsonModels.SearchResponse;
import com.search.rdbms.hibernate.models.UserEntity;
import com.search.rdbms.hibernate.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SearchServerRestController {

  private static final Logger LOG = LoggerFactory.getLogger(SearchServerRestController.class);

  @Autowired
  private EsMessageIndex esMessageIndex;

  @Autowired
  private UserRepository userRepository;

  @RequestMapping("/")
  public String index() {
    return "GroupMe Search Service is up and available.\n";
  }

  @PostMapping(value = "/newUser")
  @ResponseBody
  public void newUser(@RequestParam String username,
                      @RequestParam String accessToken) {
    LOG.debug("Received new user request with username " + username);
    UserEntity user;
    if (userRepository.existsById(username)) {
      LOG.debug("User {} exists, updating access token.", username);
      user = userRepository.getOne(username);
      user.setToken(accessToken);
    } else {
      LOG.debug("User {} does not exist, creating new user.", username);
      user = new UserEntity(username, accessToken);
    }
    userRepository.save(user);
  }

  @GetMapping(value = "/search")
  @ResponseBody
  public SearchResponse search(@RequestParam String username,
                     @RequestParam String accessToken,
                     @RequestParam String groupId,
                     @RequestParam(required = false) String name,
                     @RequestParam(required = false) String text) throws JsonProcessingException {
    LOG.debug("Received search request with username {}, groupId {}, name {}, text {}", username,
            groupId, name, text);
    // TODO secure endpoints with Spring Security
    Message messageToSearchFor = new Message(null, groupId, name, text);
    List<Message> results = esMessageIndex.searchForMessage(messageToSearchFor);
    return new SearchResponse(results.size(), results);
  }
}
