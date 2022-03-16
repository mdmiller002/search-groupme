package com.search.jsonModels.wrappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.search.jsonModels.Group;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShowGroupResponseWrapper {

  private Group response;

  public Group getResponse() {
    return response;
  }

  public void setResponse(Group response) {
    this.response = response;
  }
}
