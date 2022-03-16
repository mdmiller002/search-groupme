package com.search.groupme;

import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.search.BasicMockServerTestExtension.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupMembershipCheckerTest {

  private static final String TOKEN = "token";
  private static final String GM_API = "https://api.groupme.com/v3/groups/%s?token=%s";
  private static final String GROUP_1 = "1";
  private static final String UNAUTHORIZED_GROUP = "2";
  private static final String INVALID_GROUP = "1000";
  private static final String BAD_TOKEN = "token2";

  private static final String GROUP_1_URL = String.format(GM_API, GROUP_1, TOKEN);
  private static final String INVALID_GROUP_URL = String.format(GM_API, INVALID_GROUP, TOKEN);
  private static final String GROUP_1_BAD_URL = String.format(GM_API, GROUP_1, BAD_TOKEN);
  private static final String UNAUTHORIZED_GROUP_URL = String.format(GM_API, UNAUTHORIZED_GROUP, TOKEN);


  private GroupMembershipChecker checker;
  private GroupmeRequestMaker requestMaker;

  @BeforeEach
  public void beforeEach() throws IOException {
    setupMockRequestMaker();
    checker = new GroupMembershipChecker(ApiConstants.URL, requestMaker);
  }

  private void setupMockRequestMaker() throws IOException {
    requestMaker = mock(GroupmeRequestMaker.class);
    when(requestMaker.makeRequestWithResponseCode(new URL(GROUP_1_URL)))
        .thenReturn(new Pair<>(200, makeInputStream(SHOW_GROUP_1)));
    when(requestMaker.makeRequestWithResponseCode(new URL(INVALID_GROUP_URL)))
        .thenReturn(new Pair<>(200, makeInputStream(SHOW_GROUP_1000)));
    when(requestMaker.makeRequestWithResponseCode(new URL(GROUP_1_BAD_URL)))
        .thenReturn(new Pair<>(404, makeInputStream(UNAUTHORIZED)));
    when(requestMaker.makeRequestWithResponseCode(new URL(UNAUTHORIZED_GROUP_URL)))
        .thenReturn(new Pair<>(404, makeInputStream(UNAUTHORIZED)));

  }

  private InputStream makeInputStream(String s) {
    return new ByteArrayInputStream(s.getBytes());
  }

  @Test
  public void testUserIsMemberOfGroup() {
    assertTrue(checker.isUserMemberOfGroup(GROUP_1, TOKEN));
  }

  @Test
  public void testInvalidGroupId() {
    assertFalse(checker.isUserMemberOfGroup(INVALID_GROUP, TOKEN));
  }

  @Test
  public void testBadToken() {
    assertFalse(checker.isUserMemberOfGroup(GROUP_1, BAD_TOKEN));
  }

  @Test
  public void testUnauthorizedGroup() {
    assertFalse(checker.isUserMemberOfGroup(UNAUTHORIZED_GROUP, TOKEN));
  }
}
