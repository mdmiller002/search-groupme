package com.search.jsonModels.api;

public class ServerStatus {

  private final String message;

  public ServerStatus(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
