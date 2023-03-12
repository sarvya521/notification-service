package com.sp.notification.data;

import com.sp.notification.dto.Address;
import com.sp.notification.dto.DeliveryStatus;
import com.sp.notification.dto.MessageConfiguration;
import com.sp.notification.dto.TemplateConfiguration;
import java.time.Instant;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode(of = "id")
@Getter
@Setter
@ToString
@Table("notification")
public class Notification {

  @Id
  @Column("id")
  private Long id;

  @Column("application")
  private String application;

  @Column("sender")
  private String sender;

  @Column("is_read")
  private boolean isRead;

  @Column("created_at")
  private Instant createdAt;

  @Column("delivered_at")
  private Instant deliveredAt;

  @Column("modified_at")
  private Instant modifiedAt;

  @Column("trace_id")
  private String traceId;

  @Column("recipient_addresses")
  private String _recipientAddresses;

  @Transient
  private List<Address> recipientAddresses;

  @Column("payload")
  private String _payload;

  @Transient
  private MessageConfiguration payload;

  @Column("templates")
  private String _templates;

  @Transient
  private TemplateConfiguration templates;

  @Column("delivery_status")
  private String _deliveryStatus;

  @Transient
  private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

}
