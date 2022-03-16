package com.search.groupme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.jsonModels.wrappers.ShowGroupResponseWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriTemplate;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.search.groupme.ApiConstants.GROUP_ID;
import static com.search.groupme.ApiConstants.TOKEN;

/**
 * Provides an ability to check a user's membership in a specified group
 */
public class GroupMembershipChecker {

  private static final Logger LOG = LoggerFactory.getLogger(GroupMembershipChecker.class);

  private final String groupMeApiEndpoint;
  private final GroupmeRequestMaker requestMaker;

  public GroupMembershipChecker(String groupMeApiEndpoint) {
    this.groupMeApiEndpoint = groupMeApiEndpoint;
    requestMaker = new GroupmeRequestMaker();
  }

  public GroupMembershipChecker(String groupMeApiEndpoint, GroupmeRequestMaker requestMaker) {
    this.groupMeApiEndpoint = groupMeApiEndpoint;
    this.requestMaker = requestMaker;
  }

  public boolean isUserMemberOfGroup(String groupId, String accessToken) {
    UriTemplate uriTemplate = new UriTemplate(groupMeApiEndpoint + "/groups/{group_id}");
    Map<String,String> uriVariables = new HashMap<>();
    uriVariables.put(GROUP_ID, groupId);
    URI uri = uriTemplate.expand(uriVariables);
    URIBuilder uriBuilder = new URIBuilder(uri);
    uriBuilder.addParameter(TOKEN, accessToken);
    try {
      URL url = new URL(uriBuilder.build().toString());
      LOG.debug("Making request to: {}", url);
      Pair<Integer,InputStream> response = requestMaker.makeRequestWithResponseCode(url);
      int code = response.getValue0();
      if (code != HttpStatus.OK.value()) {
        return false;
      }
      InputStream responseStream = response.getValue1();
      ObjectMapper mapper = new ObjectMapper();
      ShowGroupResponseWrapper showGroupResponseWrapper = mapper.readValue(responseStream,
          ShowGroupResponseWrapper.class);
      if (isResponseValid(showGroupResponseWrapper)) {
        return true;
      }
    } catch (Exception e) {
      LOG.error("Error building URL", e);
    }
    return false;
  }

  /**
   * With very large Group IDs, GroupMe returns a 200 OK with an Group response full of empty fields
   */
  private boolean isResponseValid(ShowGroupResponseWrapper showGroupResponseWrapper) {
    return showGroupResponseWrapper != null &&
        showGroupResponseWrapper.getResponse() != null &&
        !StringUtils.isEmpty(showGroupResponseWrapper.getResponse().getId());
  }
}
