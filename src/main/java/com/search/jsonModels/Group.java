package com.search.jsonModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {

  private String id;
  private String name;

  public Group() { }

  public Group(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return name + " (" + id + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Group group = (Group) o;
    return id.equals(group.id) &&
        Objects.equals(name, group.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }
}
