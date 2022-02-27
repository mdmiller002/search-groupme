package com.search.jsonModels.api;

import com.search.jsonModels.Message;

import java.util.List;

public class MessagesResponse {

  private final long numResults;
  private final List<Message> results;

  public MessagesResponse(long numResults, List<Message> results) {
    this.numResults = numResults;
    this.results = results;
  }

  public long getNumResults() {
    return numResults;
  }

  public List<Message> getResults() {
    return results;
  }
}
