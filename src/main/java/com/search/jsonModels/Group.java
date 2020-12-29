package com.search.jsonModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {

  private long id;

  public void setId(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

}
