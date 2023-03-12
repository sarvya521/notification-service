package com.sp.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.notification.exception.ErrorGenerator;
import com.sp.notification.exception.NotificationExceptionHandler;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@EnableTransactionManagement
public class Config {

//  @Bean
//  ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
//    ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
//    initializer.setConnectionFactory(connectionFactory);
//    initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
//    return initializer;
//  }

  @Bean
  ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
    return new R2dbcTransactionManager(connectionFactory);
  }

  @Bean
  @Order(-2)
  NotificationExceptionHandler reactiveExceptionHandler(WebProperties webProperties,
      ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
    NotificationExceptionHandler exceptionHandler = new NotificationExceptionHandler(
        new DefaultErrorAttributes(), webProperties.getResources(), applicationContext
    );
    exceptionHandler.setMessageWriters(configurer.getWriters());
    exceptionHandler.setMessageReaders(configurer.getReaders());
    return exceptionHandler;
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    return mapper;
  }

  @Bean
  MessageSource errorMessageSource() {
    ReloadableResourceBundleMessageSource messageSource
        = new ReloadableResourceBundleMessageSource();
    messageSource.setBasename("classpath:errors-list");
    messageSource.setDefaultEncoding("UTF-8");

    ErrorGenerator.setErrorMessageSource(messageSource);

    return messageSource;
  }

}
