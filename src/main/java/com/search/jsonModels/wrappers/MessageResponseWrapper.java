package com.search.jsonModels.wrappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageResponseWrapper {

  private MessageResponse response;

  public MessageResponseWrapper() { }

  public MessageResponseWrapper(MessageResponse response) {
    this.response = response;
  }

  public MessageResponse getResponse() {
    return response;
  }

  public void setResponse(MessageResponse response) {
    this.response = response;
  }
}
