package com.sp.notification.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.notification.dto.Address;
import com.sp.notification.dto.DeliveryStatus;
import com.sp.notification.dto.MessageConfiguration;
import com.sp.notification.dto.TemplateConfiguration;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.data.r2dbc.mapping.event.AfterConvertCallback;
import org.springframework.data.r2dbc.mapping.event.AfterSaveCallback;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.r2dbc.mapping.event.BeforeSaveCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class NotificationCallback implements BeforeConvertCallback<Notification>,
    BeforeSaveCallback<Notification>,
    AfterSaveCallback<Notification>, AfterConvertCallback<Notification> {

  private final ObjectMapper mapper;

  @Override
  public Publisher<Notification> onBeforeConvert(Notification entity, SqlIdentifier table) {
    if (Objects.isNull(entity.getCreatedAt())) {
      entity.setCreatedAt(Instant.now());
    }
    entity.set_deliveryStatus(entity.getDeliveryStatus().toString());
    return Mono.just(entity);
  }

  @Override
  public Publisher<Notification> onBeforeSave(Notification entity, OutboundRow row,
      SqlIdentifier table) {
    row.put(SqlIdentifier.unquoted("delivery_status"),
        Parameter.from(entity.getDeliveryStatus().toString()));

    StringWriter writer = new StringWriter();
    try {
      mapper.writeValue(writer, entity.getRecipientAddresses());
      writer.flush();
      row.put(SqlIdentifier.unquoted("recipient_addresses"), Parameter.from(writer.toString()));

      writer = new StringWriter();
      mapper.writeValue(writer, entity.getPayload());
      writer.flush();
      row.put(SqlIdentifier.unquoted("payload"), Parameter.from(writer.toString()));

      if (Objects.nonNull(entity.getTemplates())) {
        writer = new StringWriter();
        mapper.writeValue(writer, entity.getTemplates());
        writer.flush();
        row.put(SqlIdentifier.unquoted("templates"), Parameter.from(writer.toString()));
      }
    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Failed to convert object to String: %s", e.getMessage()));
    }

    return Mono.just(entity);
  }

  @Override
  public Publisher<Notification> onAfterSave(Notification entity, OutboundRow outboundRow,
      SqlIdentifier table) {
    if (outboundRow.get("delivery_status").hasValue()) {
      entity.setDeliveryStatus(
          DeliveryStatus.valueOf(outboundRow.get("delivery_status").getValue().toString()));
    }

    try {
      if (outboundRow.get("recipient_addresses").hasValue()) {
        String payloadJson = outboundRow.get("recipient_addresses").getValue().toString();
        entity.setRecipientAddresses(mapper.readValue(payloadJson.getBytes(StandardCharsets.UTF_8),
            new TypeReference<List<Address>>() {
              @Override
              public Type getType() {
                return super.getType();
              }
            }));
      }

      if (outboundRow.get("payload").hasValue()) {
        String payloadJson = outboundRow.get("payload").getValue().toString();
        entity.setPayload(mapper.readValue(payloadJson.getBytes(StandardCharsets.UTF_8),
            MessageConfiguration.class));
      }

      if (outboundRow.get("templates").hasValue()) {
        String payloadJson = outboundRow.get("templates").getValue().toString();
        entity.setTemplates(mapper.readValue(payloadJson.getBytes(StandardCharsets.UTF_8),
            TemplateConfiguration.class));
      }
    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Failed to convert String to Object: %s", e.getMessage()));
    }

    return Mono.just(entity);
  }

  @Override
  public Publisher<Notification> onAfterConvert(Notification entity, SqlIdentifier table) {
    try {
      entity.setRecipientAddresses(
          mapper.readValue(entity.get_recipientAddresses().getBytes(StandardCharsets.UTF_8),
              new TypeReference<List<Address>>() {
                @Override
                public Type getType() {
                  return super.getType();
                }
              }));
      entity.setPayload(mapper.readValue(entity.get_payload().getBytes(StandardCharsets.UTF_8),
          MessageConfiguration.class));
      String ctemplates = entity.get_templates();
      if (Objects.nonNull(ctemplates)) {
        entity.setTemplates(mapper.readValue(ctemplates.getBytes(StandardCharsets.UTF_8),
            TemplateConfiguration.class));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    entity.setDeliveryStatus(DeliveryStatus.valueOf(entity.get_deliveryStatus()));
    return Mono.just(entity);
  }
}