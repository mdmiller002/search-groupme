package com.search.groupme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.jsonModels.Message;
import com.search.jsonModels.Group;
import com.search.jsonModels.wrappers.GroupResponseWrapper;
import com.search.jsonModels.wrappers.MessageResponseWrapper;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GroupMeInterface {

  private static final Logger LOG = LoggerFactory.getLogger(GroupMeInterface.class);

  private final String accessToken;

  public GroupMeInterface(String accessToken) {
    this.accessToken = accessToken;
  }

  public Optional<List<Message>> getMessages(long groupId) {
    try {
      URIBuilder uriBuilder = new URIBuilder(getUriForMessages(groupId));
      uriBuilder.addParameter("token", accessToken);
      URL url = new URL(uriBuilder.build().toString());
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestProperty("Content-Type", "application/json");
      InputStream responseStream = con.getInputStream();
      ObjectMapper mapper = new ObjectMapper();
      MessageResponseWrapper messageResponseWrapper = mapper.readValue(responseStream, MessageResponseWrapper.class);

      return Optional.of(messageResponseWrapper.getResponse().getMessages());
    } catch (IOException | URISyntaxException e) {
      LOG.error("Error getting messages", e);
    }
    return Optional.empty();
  }

  private URI getUriForMessages(long groupId) {
    UriTemplate uriTemplate = new UriTemplate("https://api.groupme.com/v3/groups/{group_id}/messages");
    Map<String, String> uriVariables = new HashMap<>();
    uriVariables.put("group_id", String.valueOf(groupId));
    return uriTemplate.expand(uriVariables);
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
