package com.search;

import com.search.elasticsearch.RestClientManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class SearchServerRestController {

  @Autowired
  RestClientManager clientManager;

  @RequestMapping("/")
  public String index() {
    return "Greetings from Spring Boot!";
  }

}
