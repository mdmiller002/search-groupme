package com.search.groupme;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class RESTController {

  @RequestMapping("/")
  public String index() {
    return "Greetings from Spring Boot!";
  }

}