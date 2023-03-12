package com.sp.notification;

import com.sp.notification.dto.ApiStatus;
import com.sp.notification.dto.Identifier;
import com.sp.notification.dto.MessagePayload;
import com.sp.notification.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class NotificationController {

  @Autowired
  private NotificationService service;

  @PostMapping(value = "/v1/notify", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<Response<Identifier>>> sendNotification(
      @RequestBody MessagePayload messagePayload) {
    return service.createNotification(messagePayload)
        .map(notificationId -> {
          log.debug("Notification {} created", notificationId);
          return new ResponseEntity<>(
              new Response<>(ApiStatus.SUCCESS, HttpStatus.CREATED.value(),
                  new Identifier(notificationId)),
              HttpStatus.CREATED);
        });
  }
}
