package com.sp.notification.exception;

import com.sp.notification.dto.ApiStatus;
import com.sp.notification.dto.ErrorDetails;
import com.sp.notification.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
public class NotificationExceptionHandler extends AbstractErrorWebExceptionHandler {

  public NotificationExceptionHandler(ErrorAttributes errorAttributes,
      WebProperties.Resources resources,
      ApplicationContext applicationContext) {
    super(errorAttributes, resources, applicationContext);
  }

  @Override
  protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
    return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
  }

  private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
    Throwable error = getError(request);
    log.error("Exception", error);
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    Response response;
    if (error instanceof NotificationException notificationException) {
      ErrorDetails errorDetails = notificationException.getError();
      response = new Response<Void>(ApiStatus.CLIENT_ERROR, httpStatus.value(), errorDetails);
    } else {
      response = new Response<Void>(ApiStatus.FAIL, httpStatus.value(),
          ErrorGenerator.generateForCode("9000"));
    }

    return ServerResponse
        .status(httpStatus)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(response)
        );
  }
}
