package com.sp.notification;

import io.r2dbc.spi.ConnectionFactoryOptions;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.MySQLR2DBCDatabaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class AbstractIT {

  static final MySQLContainer MYSQL_CONTAINER = new MySQLContainer("mysql:8.0.29");

  static final MySQLR2DBCDatabaseContainer MYSQL_R2DB_CONTAINER = new MySQLR2DBCDatabaseContainer(
      MYSQL_CONTAINER);

  static {
    MYSQL_CONTAINER.withStartupTimeout(Duration.of(5, ChronoUnit.MINUTES)).start();
    MYSQL_R2DB_CONTAINER.start();
  }

  @DynamicPropertySource
  public static void properties(DynamicPropertyRegistry registry) {
    ConnectionFactoryOptions options = MySQLR2DBCDatabaseContainer.getOptions(MYSQL_CONTAINER);
    registry.add("spring.r2dbc.url", () -> "r2dbc:mysql://"
        + MYSQL_CONTAINER.getHost() + ":" + MYSQL_CONTAINER.getFirstMappedPort()
        + "/" + MYSQL_CONTAINER.getDatabaseName()
        + "?serverTimezone=UTC");
    registry.add("spring.r2dbc.username", MYSQL_CONTAINER::getUsername);
    registry.add("spring.r2dbc.password", MYSQL_CONTAINER::getPassword);
  }
}
