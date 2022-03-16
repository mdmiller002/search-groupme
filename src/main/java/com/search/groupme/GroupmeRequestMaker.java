package com.search.groupme;

import org.apache.http.entity.ContentType;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Mock-able object that makes the HTTP requests to
 * GroupMe's API
 */
public class GroupmeRequestMaker {

  private static final Logger LOG = LoggerFactory.getLogger(GroupmeRequestMaker.class);
  private static final String CONTENT_TYPE = "Content-Type";

  public GroupmeRequestMaker() { }

  public InputStream makeRequest(URL url) throws IOException {
    return makeRequest(url, ContentType.APPLICATION_JSON);
  }

  public InputStream makeRequest(URL url, ContentType contentType) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty(CONTENT_TYPE, contentType.getMimeType());
    return connection.getInputStream();
  }

  public Pair<Integer,InputStream> makeRequestWithResponseCode(URL url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty(CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    connection.connect();
    int code = connection.getResponseCode();
    InputStream response;
    try {
      response = connection.getInputStream();
    } catch (Exception e) {
      LOG.debug("Error getting input stream from connection:", e);
      response = null;
    }
    return new Pair<>(code, response);
  }
}
