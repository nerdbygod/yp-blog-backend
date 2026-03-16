package org.practicum.yandex.controller.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@ControllerAdvice
public class ResponseExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatus(ResponseStatusException rse) {
        return new ResponseEntity<>(rse.getReason(), rse.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleRuntimeException(Exception exception,
                                                         WebRequest request) {
        log.warn("{} Unhandled error occurred while processing request: ", request.getDescription(false), exception);

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unhandled error");
    }

}
