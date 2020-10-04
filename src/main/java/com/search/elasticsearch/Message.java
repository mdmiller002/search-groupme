package com.search.elasticsearch;

/**
 * Message is a single message sent in a group that can
 * be serialized/deserialized into JSON for Elasticsearch
 */
public class Message {

  private String sender;
  private String text;

  public Message() {
    super();
  }

  public Message(String sender, String text) {
    this.sender = sender;
    this.text = text;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return "Sender: [" + getSender() + "] Text: [" + getText() + "]";
  }
}
