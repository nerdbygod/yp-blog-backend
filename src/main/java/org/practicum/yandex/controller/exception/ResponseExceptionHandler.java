package org.practicum.yandex.controller.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.UUID;

@Slf4j
@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatus(ResponseStatusException rse) {
        return new ResponseEntity<>(rse.getReason(), rse.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception exception,
                                                         WebRequest request) {
        final var uuid = UUID.randomUUID().toString();

        log.warn("[{}] [{}] Unhandled error occurred while processing request",
                request.getDescription(false), uuid, exception
        );

        final var body = ErrorDto.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .message("Unhandled exception occurred")
                .id(uuid)
                .build();

        return handleExceptionInternal(exception, body, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

}
