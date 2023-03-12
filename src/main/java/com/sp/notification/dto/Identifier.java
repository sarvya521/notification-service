package com.sp.notification.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Identifier(Long id) {

  @JsonCreator
  public Identifier(@JsonProperty("id") Long id) {
    this.id = id;
  }
}

