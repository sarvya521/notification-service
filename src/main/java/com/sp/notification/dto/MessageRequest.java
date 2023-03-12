package com.sp.notification.dto;

import java.util.List;

public record MessageRequest(List<Address> addresses, MessageConfiguration messageConfiguration,
                             TemplateConfiguration templateConfiguration, String traceId) {

}
