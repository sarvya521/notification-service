package com.sp.notification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class NotificationServiceApplicationTests extends AbstractIT {

  @Autowired
  private ApplicationContext context;

  @Test
  void whenSpringContextIsBootstrapped_thenNoExceptions() {
    assertThat(this.context).isNotNull();
  }

}
