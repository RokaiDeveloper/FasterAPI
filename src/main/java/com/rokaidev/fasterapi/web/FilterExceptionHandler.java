package com.rokaidev.fasterapi.web;

import com.rokaidev.fasterapi.persistence.FilterValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class FilterExceptionHandler {

    @ExceptionHandler(FilterValidationException.class)
    public ResponseEntity<String> handleFilterValidationException(FilterValidationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}
