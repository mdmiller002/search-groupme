package com.search.jsonModels;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An EsMessageDocument contains the full contents of a Message,
 * as well as some additional data stored specifically for Elasticsearch
 * (for e.g., a duplicated name/name_keyword value so that the username
 * can be full-text searchable as well as exact matchable in ES)
 */
public class EsMessageDocument extends Message {

  public static final String NAME_KEYWORD_KEY = "name_keyword";

  public EsMessageDocument() { }

  public EsMessageDocument(Message message) {
    super(message.getId(), message.getGroupId(), message.getName(), message.getText());
  }

  /**
   * Duplicate the sender name when serializing the object, so that we can persist
   * both a text-searchable name, and a keyword-matchable name.
   */
  @JsonProperty(NAME_KEYWORD_KEY)
  public String getNameKeyword() {
    return getName();
  }
}
