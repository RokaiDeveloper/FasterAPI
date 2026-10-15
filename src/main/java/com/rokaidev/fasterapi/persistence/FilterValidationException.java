package com.rokaidev.fasterapi.persistence;

public class FilterValidationException extends RuntimeException {
    
    public FilterValidationException(String message) {
        super(message);
    }
    
    public FilterValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
