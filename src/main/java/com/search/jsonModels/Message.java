package com.search.jsonModels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;

/**
 * Message is a single message sent in a group that can
 * be serialized/deserialized into JSON for Elasticsearch
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

  private String id;
  @JsonProperty(GROUP_ID_KEY)
  private String groupId;
  private String name;
  private String text;

  public static final String ID_KEY = "id";
  public static final String GROUP_ID_KEY = "group_id";
  public static final String NAME_KEY = "name";
  public static final String TEXT_KEY = "text";

  public Message() { }

  public Message(String id, String groupId, String name, String text) {
    this.id = id;
    this.groupId = groupId;
    this.name = name;
    this.text = text;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return getName() + ": " + getText() + " (id: " + id +", groupId: " + groupId + ")";
  }

  @JsonIgnore
  public Optional<String> getDocId() {
    if (getId() == null || getGroupId() == null) {
      return Optional.empty();
    }
    return Optional.of(getGroupId() + "_" + getId());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Message message = (Message) o;
    return Objects.equals(id, message.id) &&
        Objects.equals(groupId, message.groupId) &&
        Objects.equals(name, message.name) &&
        Objects.equals(text, message.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, groupId, name, text);
  }
}
