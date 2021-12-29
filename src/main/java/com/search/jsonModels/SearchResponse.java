package com.search.jsonModels;

import java.util.List;

public class SearchResponse {

  private final long numResults;
  private final List<Message> results;

  public SearchResponse(long numResults, List<Message> results) {
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
