package com.rokaidev.fasterapi.web;

import com.rokaidev.fasterapi.persistence.CrudRegistration;
import com.rokaidev.fasterapi.persistence.CrudRegistrationRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;

@ControllerAdvice
public class CrudDisabledOperationHandler {

    private final CrudRegistrationRegistry registry;

    public CrudDisabledOperationHandler(CrudRegistrationRegistry registry) {
        this.registry = registry;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Void> handleUnsupportedMethod(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {
        if (isCrudPath(request)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    private boolean isCrudPath(HttpServletRequest request) {
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        String normalizedPath = removeTrailingSlash(requestPath);

        return registry.getRegistrations().stream()
                .map(CrudRegistration::path)
                .map(this::removeTrailingSlash)
                .anyMatch(path -> normalizedPath.equals(path)
                        || normalizedPath.startsWith(path + "/")
                        && normalizedPath.indexOf('/', path.length() + 1) < 0);
    }

    private String removeTrailingSlash(String path) {
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}
