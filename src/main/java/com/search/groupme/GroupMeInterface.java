package com.search.groupme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.groupme.json.Group;
import com.search.groupme.json.GroupResponseWrapper;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class GroupMeInterface {

  private static final Logger LOG = LoggerFactory.getLogger(GroupMeInterface.class);

  private final String accessToken;

  public GroupMeInterface(String accessToken) {
    this.accessToken = accessToken;
  }

  public Optional<List<Group>> getGroups() {
    try {
      URIBuilder uriBuilder = new URIBuilder("https://api.groupme.com/v3/groups");
      uriBuilder.addParameter("token", accessToken);
      URL url = new URL(uriBuilder.build().toString());
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestProperty("Content-Type", "application/json");
      InputStream responseStream = con.getInputStream();
      ObjectMapper mapper = new ObjectMapper();

      GroupResponseWrapper groupResponseWrapper = mapper.readValue(responseStream, GroupResponseWrapper.class);
      return Optional.of(groupResponseWrapper.getResponse());
    } catch (URISyntaxException | IOException e) {
      LOG.error("Error getting groups", e);
    }
    return Optional.empty();
  }

}
