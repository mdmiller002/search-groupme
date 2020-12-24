package com.search.rdbms.hibernate.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * User entity
 * Maps to the user table in the RDBMS, which
 * stores some necessary state about users
 */
@Entity
public class User {

  @Id
  @GeneratedValue
  private Long id;
  private String username;
  private String token;

  public User() { }

  public User(String username, String token) {
    this.username = username;
    this.token = token;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
