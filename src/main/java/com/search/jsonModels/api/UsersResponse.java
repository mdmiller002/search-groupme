package com.search.jsonModels.api;

public class UsersResponse {
  private final String message;

  public UsersResponse(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
