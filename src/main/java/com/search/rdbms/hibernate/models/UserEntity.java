package com.search.rdbms.hibernate.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * User entity
 * Maps to the user table in the RDBMS, which
 * stores some necessary state about users
 */
@Entity
@Table(name = UserEntity.TABLE_NAME)
public class UserEntity {

  public static final String TABLE_NAME = "USERS";

  @Id
  private String username;
  private String token;

  public UserEntity() { }

  public UserEntity(String username, String token) {
    this.username = username;
    this.token = token;
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
