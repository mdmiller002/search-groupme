package com.search.rdbms.hibernate.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = GroupEntity.TABLE_NAME)
public class GroupEntity {

  public static final String TABLE_NAME = "GROUPS";

  @Id
  long id;
  long mostRecentMessageId;

  public GroupEntity() { }

  public GroupEntity(long id, long mostRecentMessageId) {
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
