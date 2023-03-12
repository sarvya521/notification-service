package com.sp.notification.exception;

import com.sp.notification.dto.ErrorDetails;
import lombok.Getter;

public class NotificationException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * see {@link ErrorDetails}
   */
  @Getter
  private final ErrorDetails error;


  /**
   * @param error ErrorDetails
   * @see RuntimeException#RuntimeException(String)
   */
  public NotificationException(ErrorDetails error) {
    super(error.toString());
    this.error = error;
  }

  /**
   * @param error ErrorDetails
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *              (A {@code null} value is permitted, and indicates that the cause is nonexistent or
   *              unknown.)
   * @see RuntimeException#RuntimeException(String, Throwable)
   */
  public NotificationException(ErrorDetails error, Throwable cause) {
    super(error.toString(), cause);
    this.error = error;
  }
}
