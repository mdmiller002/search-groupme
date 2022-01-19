package com.search.jsonModels;

public class ServerStatus {

  private final String message;

  public ServerStatus(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
