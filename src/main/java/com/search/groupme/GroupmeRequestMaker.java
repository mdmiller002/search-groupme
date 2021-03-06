package com.search.groupme;

import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Mock-able object that makes the HTTP requests to
 * GroupMe's API
 */
public class GroupmeRequestMaker {

  private static final String CONTENT_TYPE = "Content-Type";

  public GroupmeRequestMaker() { }

  public InputStream makeRequest(URL url) throws IOException {
    return makeRequest(url, ContentType.APPLICATION_JSON);
  }

  public InputStream makeRequest(URL url, ContentType contentType) throws IOException {
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestProperty(CONTENT_TYPE, contentType.getMimeType());
    return con.getInputStream();
  }

}
