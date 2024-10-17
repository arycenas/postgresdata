package com.training.postgresdata.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", exception.getStatusCode().value());
        response.put("reason", exception.getReason());

        return new ResponseEntity<>(response, exception.getStatusCode());
    }
}
