package com.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class SearchServer {

  private static final Logger LOG = LoggerFactory.getLogger(SearchServer.class);

  public static void main(String[] args) {
    SpringApplication.run(SearchServer.class, args);
  }

  @Bean
  public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    return args -> LOG.info("Running Search Server service with Spring profiles {}",
        String.join(",", ctx.getBean(Environment.class).getActiveProfiles()));
  }

}
