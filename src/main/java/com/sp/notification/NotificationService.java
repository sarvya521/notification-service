package com.sp.notification;

import com.sp.notification.data.Notification;
import com.sp.notification.data.NotificationRepository;
import com.sp.notification.dto.Address;
import com.sp.notification.dto.Channel;
import com.sp.notification.dto.DeliveryStatus;
import com.sp.notification.dto.EmailMessage;
import com.sp.notification.dto.MessagePayload;
import com.sp.notification.dto.MessageRequest;
import com.sp.notification.dto.SimpleEmail;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

  private final JavaMailSender client;
  private final NotificationRepository repo;

  private Mono<Long> updateNotificationDelivery(Long notificationId) {
    return repo.findById(notificationId)
        .flatMap(persistedNotification -> {
          persistedNotification.setDeliveredAt(Instant.now());
          persistedNotification.setDeliveryStatus(DeliveryStatus.SENT);
          return repo.save(persistedNotification);
        })
        .flatMap(updatedNotification -> Mono.just(updatedNotification.getId()))
        .doOnNext(updatedNotification -> log.debug("email notification updated {}",
            updatedNotification));
  }

  private Mono<Long> sendEmail(Long notificationId, MessageRequest messageRequest) {
    boolean isEmailRequest = messageRequest.addresses().stream()
        .anyMatch(address -> Objects.equals(Channel.EMAIL, address.channel()));
    if (!isEmailRequest) {
      return Mono.empty();
    }
    EmailMessage emailMessage = messageRequest.messageConfiguration().emailMessage();
    SimpleEmail simpleEmail = emailMessage.simpleEmail();

    String[] toEmailAddresses = messageRequest.addresses().stream()
        .filter(address -> Objects.equals(Channel.EMAIL, address.channel()))
        .map(Address::address)
        .toArray(String[]::new);

    if (Objects.nonNull(simpleEmail.textPart())) {
      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setTo(toEmailAddresses);
      mailMessage.setFrom(emailMessage.fromAddress());
      mailMessage.setSubject(simpleEmail.subject().data());
      mailMessage.setText(simpleEmail.textPart().data());
      return Mono.defer(() -> {
        client.send(mailMessage);
        return Mono.just(notificationId);
      }).retryWhen(Retry.backoff(3, Duration.ofSeconds(2)));
    } else {
      return Mono.defer(() -> {
        client.send(mimeMessage -> {
          MimeMessageHelper helper =
              new MimeMessageHelper(mimeMessage, false, "UTF-8");
          helper.setTo(toEmailAddresses);
          helper.setFrom(emailMessage.fromAddress());
          helper.setSubject(simpleEmail.subject().data());
          helper.setText(simpleEmail.htmlPart().data(), true);
        });
        return Mono.just(notificationId);
      }).retryWhen(Retry.backoff(3, Duration.ofSeconds(2)));
    }
  }

  private Mono<Void> sendMsTeamsMessage(MessageRequest messageRequest) {
    boolean isMsTeamsRequest = messageRequest.addresses().stream()
        .anyMatch(address -> Objects.equals(Channel.MSTEAMS, address.channel()));
    if (!isMsTeamsRequest) {
      return Mono.empty();
    }
    return Mono.empty();
  }

  public Mono<Long> createNotification(MessagePayload messagePayload) {
    Notification notification = new Notification();
    notification.setApplication(messagePayload.application());
    notification.setSender("00000000-0000-0000-0000-000000000000"); //TODO: set from AuthUtil
    notification.setRecipientAddresses(messagePayload.messageRequest().addresses());
    notification.setPayload(messagePayload.messageRequest().messageConfiguration());
    notification.setTemplates(messagePayload.messageRequest().templateConfiguration());

    return repo.save(notification)
        .flatMap(persistedNotification -> Mono.just(persistedNotification.getId()))
        .publishOn(Schedulers.boundedElastic())
        .doOnSuccess(id ->
            sendEmail(id, messagePayload.messageRequest())
                .onErrorComplete()
                .flatMap(this::updateNotificationDelivery)
                .subscribe()
        );
  }

}
