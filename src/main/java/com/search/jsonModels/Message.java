package com.search.jsonModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Message is a single message sent in a group that can
 * be serialized/deserialized into JSON for Elasticsearch
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

  private String name;
  private String text;

  public static final String NAME_KEY = "name";
  public static final String TEXT_KEY = "text";

  public Message() { }

  public Message(String name, String text) {
    this.name = name;
    this.text = text;
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
    return "Name: [" + getName() + "] Text: [" + getText() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Message)) {
      return false;
    }
    Message m = (Message) o;
    return name.equals(m.name) && text.equals(m.text);
  }
}
