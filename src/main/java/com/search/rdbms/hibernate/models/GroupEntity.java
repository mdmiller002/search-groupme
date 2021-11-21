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
  private String id;
  private String topPointer;
  private String bottomPointer;
  private boolean isInitialized;

  public GroupEntity() { }

  public GroupEntity(String id, String topPointer, String bottomPointer, boolean isInitialized) {
    this.id = id;
    this.topPointer = topPointer;
    this.bottomPointer = bottomPointer;
    this.isInitialized = isInitialized;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTopPointer() {
    return topPointer;
  }

  public void setTopPointer(String topPointer) {
    this.topPointer = topPointer;
  }

  public String getBottomPointer() {
    return bottomPointer;
  }

  public void setBottomPointer(String bottomPointer) {
    this.bottomPointer = bottomPointer;
  }

  public boolean isInitialized() {
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
