package com.search;

import com.search.elasticsearch.RestClientManager;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SearchSpringConfigTest {

  ApplicationContext applicationContext;

  @BeforeEach
  public void beforeEach() {
    applicationContext = new AnnotationConfigApplicationContext(SearchSpringConfig.class);
  }

  @Test
  public void testRestClientManager() throws IOException {
    RestClientManager restClientManager = applicationContext.getBean(RestClientManager.class);
    assertNotNull(restClientManager);
    RestHighLevelClient client = restClientManager.get();
    MainResponse response = client.info(RequestOptions.DEFAULT);
    assertNotNull(response);
    assertEquals("docker-cluster", response.getClusterName());
    restClientManager.close();
  }

}
