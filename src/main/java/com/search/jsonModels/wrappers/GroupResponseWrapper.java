package com.search.jsonModels.wrappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.search.jsonModels.Group;

import java.util.List;

/**
 * Group response comes back as an array wrapped inside
 * a "response" Json object. Use a wrapper Json object to
 * effectively deserialize the Group response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupResponseWrapper {

  private List<Group> response;

  public List<Group> getResponse() {
    return response;
  }

  public void setResponse(List<Group> response) {
    this.response = response;
  }
}
