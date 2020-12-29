package com.search.rdbms.hibernate.models;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Group {

  @Id
  long id;
  long mostRecentMessageId;

  public Group() { }

  public Group(long id, long mostRecentMessageId) {
    this.id = id;
    this.mostRecentMessageId = mostRecentMessageId;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getMostRecentMessageId() {
    return mostRecentMessageId;
  }

  public void setMostRecentMessageId(long mostRecentMessageId) {
    this.mostRecentMessageId = mostRecentMessageId;
  }
}
