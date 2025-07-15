package com.archmanager_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jRepositoriesAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
    Neo4jDataAutoConfiguration.class,
    Neo4jRepositoriesAutoConfiguration.class
})
@EnableScheduling
public class ArchManagerBackApplication {

  public static void main(String[] args) {
    SpringApplication.run(ArchManagerBackApplication.class, args);
  }

}
