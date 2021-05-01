package com.search.rdbms.hibernate.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = GroupEntity.TABLE_NAME)
public class GroupEntity {

  public static final String TABLE_NAME = "GROUPS";

  @Id
  Long id;
  Long topPointer;
  Long bottomPointer;

  boolean isInitialized;

  public GroupEntity() { }

  public GroupEntity(Long id, Long topPointer, Long bottomPointer, boolean isInitialized) {
    this.id = id;
    this.topPointer = topPointer;
    this.bottomPointer = bottomPointer;
    this.isInitialized = isInitialized;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getTopPointer() {
    return topPointer;
  }

  public void setTopPointer(Long topPointer) {
    this.topPointer = topPointer;
  }

  public Long getBottomPointer() {
    return bottomPointer;
  }

  public void setBottomPointer(Long bottomPointer) {
    this.bottomPointer = bottomPointer;
  }

  public boolean getInitialized() {
    return isInitialized;
  }

  public void setInitialized(boolean isInitialized) {
    this.isInitialized = isInitialized;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GroupEntity that = (GroupEntity) o;
    return id.equals(that.id) &&
        Objects.equals(topPointer, that.topPointer) &&
        Objects.equals(bottomPointer, that.bottomPointer) &&
        Objects.equals(isInitialized, that.isInitialized);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, topPointer, bottomPointer, isInitialized);
  }
}
