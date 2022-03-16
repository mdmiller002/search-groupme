package com.search;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.http.HttpStatus;

import static com.search.IntegrationTestConstants.TOKEN1;
import static com.search.IntegrationTestConstants.TOKEN2;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.Parameter.param;

/**
 * Test extension to provide some basic functionality from a mock server
 * for integration tests to work.
 */
public class BasicMockServerTestExtension implements BeforeAllCallback, AfterAllCallback {

  private static final int PORT = 1090;

  public static final String SHOW_GROUP_1 = """
      {"meta":{"code":200},
      "response":{"id":"1","name":"Group 1","phone_number":"+1000000000"}}
      """;
  public static final String SHOW_GROUP_2 = """
      {"meta":{"code":200},
      "response":{"id":"2","name":"Group 2","phone_number":"+1000000000"}}
      """;
  public static final String SHOW_GROUP_1000 = """
      {"meta":{"code":200},
      "response":{"id":"","name":"","phone_number":null}}
      """;
  public static final String UNAUTHORIZED = """
      {"meta":{"code":404,"errors":["not found"]}}""";

  private static final String TOKEN_GROUP_REGEX = "(" + TOKEN1 + "|" + TOKEN2 + ")";

  private ClientAndServer mockServer;

  @Override
  public void beforeAll(ExtensionContext context) {
    mockServer = new ClientAndServer(PORT);

    // Match /groups/1 with token1 and token2
    mockServer
        .when(
            request()
                .withMethod("GET")
                .withPath("/groups/1")
                .withQueryStringParameter(param("token", TOKEN_GROUP_REGEX))
        )
        .respond(
            response()
                .withStatusCode(HttpStatus.OK.value())
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(SHOW_GROUP_1)
        );

    // Match /groups/2 with token1 and token2
    mockServer
        .when(
            request()
                .withMethod("GET")
                .withPath("/groups/2")
                .withQueryStringParameter(param("token", TOKEN_GROUP_REGEX))
        )
        .respond(
            response()
                .withStatusCode(HttpStatus.OK.value())
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(SHOW_GROUP_2)
        );

    // GroupMe returns a 200 OK with an empty response for some high group IDs
    mockServer
        .when(
            request()
                .withMethod("GET")
                .withPath("groups/1000")
                .withQueryStringParameter(param("token", ".*"))
        )
        .respond(
            response()
                .withStatusCode(HttpStatus.OK.value())
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(SHOW_GROUP_1000)
        );

    // All other groups and tokens get a 404
    // In mockserver, expectations created first take precedence
    mockServer
        .when(
            request()
                .withMethod("GET")
                .withPath("/groups/\\d*")
                .withQueryStringParameter(param("token", ".*"))
        )
        .respond(
            response()
                .withStatusCode(HttpStatus.NOT_FOUND.value())
                .withContentType(MediaType.APPLICATION_JSON)
                .withBody(UNAUTHORIZED)
        );
  }

  @Override
  public void afterAll(ExtensionContext context) {
    mockServer.stop();
  }
}
