package com.search.jsonModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

/**
 * Message is a single message sent in a group that can
 * be serialized/deserialized into JSON for Elasticsearch
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

  private long id;
  private String name;
  private String text;

  public static final String ID_KEY = "id";
  public static final String NAME_KEY = "name";
  public static final String TEXT_KEY = "text";

  public Message() { }

  public Message(long id, String name, String text) {
    this.id = id;
    this.name = name;
    this.text = text;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
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
    return getName() + ": " + getText() + " (id: " + id +")";
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Message message = (Message) o;
    return Objects.equals(id, message.id) &&
        Objects.equals(name, message.name) &&
        Objects.equals(text, message.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, text);
  }
}
