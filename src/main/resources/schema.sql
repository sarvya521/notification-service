CREATE TABLE IF NOT EXISTS t_notification
(
    c_id                  BIGINT                             NOT NULL PRIMARY KEY AUTO_INCREMENT,
    c_application         VARCHAR(255)                       NOT NULL,
    c_payload             JSON                               NOT NULL,
    c_recipient_addresses JSON                               NOT NULL,
    c_templates           JSON,
    c_delivery_status     ENUM ("PENDING", "SENT", "FAILED") NOT NULL,
    c_sender              VARCHAR(255)                       NOT NULL,
    c_trace_id            VARCHAR(50),
    c_is_read             TINYINT(1) DEFAULT 0,
    c_created_at          TIMESTAMP                          NOT NULL,
    c_delivered_at        TIMESTAMP,
    c_modified_at         TIMESTAMP  DEFAULT CURRENT_TIMESTAMP
);