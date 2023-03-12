package com.sp.notification.dto;

public record SimpleEmail(SimpleEmailPart subject, SimpleEmailPart htmlPart,
                          SimpleEmailPart textPart) {

}
