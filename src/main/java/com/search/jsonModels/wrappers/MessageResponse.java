package com.search.jsonModels.wrappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.search.jsonModels.Message;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageResponse {

  private List<Message> messages;

  public MessageResponse() { }

  public MessageResponse(List<Message> messages) {
    this.messages = messages;
  }

  public List<Message> getMessages() {
    return messages;
  }

  public void setMessages(List<Message> messages) {
    this.messages = messages;
  }
}
