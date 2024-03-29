package com.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.search.elasticsearch.EsMessageIndex;
import com.search.groupme.GroupMembershipChecker;
import com.search.jsonModels.Message;
import com.search.jsonModels.api.MessageUnauthorizedResponse;
import com.search.jsonModels.api.MessagesResponse;
import com.search.jsonModels.api.ServerStatus;
import com.search.jsonModels.api.UsersResponse;
import com.search.rdbms.hibernate.models.UserEntity;
import com.search.rdbms.hibernate.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SearchServerRestController {

  private static final Logger LOG = LoggerFactory.getLogger(SearchServerRestController.class);

  @Autowired
  private EsMessageIndex esMessageIndex;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private GroupMembershipChecker groupMembershipChecker;

  @RequestMapping("/")
  public ServerStatus index() {
    return new ServerStatus("GroupMe Search Service is up and available.");
  }

  @PostMapping(value = "/users")
  @ResponseBody
  public UsersResponse users(@RequestParam String username,
                             @RequestParam String accessToken) {
    LOG.debug("Received new user request with username {}", username);
    UserEntity user;
    String message;
    if (userRepository.existsById(username)) {
      LOG.debug("User {} exists, updating access token.", username);
      message = "Updated access token for " + username;
      user = userRepository.getById(username);
      user.setToken(accessToken);
    } else {
      LOG.debug("User {} does not exist, creating new user.", username);
      message = "Created new user " + username;
      user = new UserEntity(username, accessToken);
    }
    userRepository.save(user);
    return new UsersResponse(message);
  }

  @GetMapping(value = "/messages")
  @ResponseBody
  public ResponseEntity<?> messages(@RequestParam String username,
                                 @RequestParam String accessToken,
                                 @RequestParam String groupId,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String text) throws JsonProcessingException {
    LOG.debug("Received search request with username {}, groupId {}, name {}, text {}", username,
            groupId, name, text);
    if (!groupMembershipChecker.isUserMemberOfGroup(groupId, accessToken)) {
      LOG.debug("User is not a member of this group");
      return new ResponseEntity<>(new MessageUnauthorizedResponse("User is not a member of this group"),
          HttpStatus.UNAUTHORIZED);
    }
    // TODO secure endpoints with Spring Security
    Message messageToSearchFor = new Message(null, groupId, name, text);
    List<Message> results = esMessageIndex.searchForMessage(messageToSearchFor);
    return new ResponseEntity<>(new MessagesResponse(results.size(), results), HttpStatus.OK);
  }
}
