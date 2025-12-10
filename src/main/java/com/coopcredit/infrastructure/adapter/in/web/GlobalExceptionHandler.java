package com.coopcredit.infrastructure.adapter.in.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationError(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication Error: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setType(URI.create("https://coopcredit.com/errors/authentication"));
        problem.setTitle("Error de Autenticación");
        problem.setDetail("Credenciales inválidas o usuario no encontrado");
        addStandardProperties(problem, request);
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation Error: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://coopcredit.com/errors/validation"));
        problem.setTitle("Error de Validación");

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        problem.setDetail(details);
        addStandardProperties(problem, request);
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBusinessError(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Business Error: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setType(URI.create("https://coopcredit.com/errors/business-rule"));
        problem.setTitle("Violación de Regla de Negocio");
        problem.setDetail(ex.getMessage());
        addStandardProperties(problem, request);
        return problem;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException ex, HttpServletRequest request) {
        log.warn("Resource Not Found: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create("https://coopcredit.com/errors/not-found"));
        problem.setTitle("Recurso No Encontrado");
        problem.setDetail(ex.getMessage());
        addStandardProperties(problem, request);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericError(Exception ex, HttpServletRequest request) {
        log.error("Unhandled Exception at {}: ", request.getRequestURI(), ex);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create("https://coopcredit.com/errors/internal"));
        problem.setTitle("Error Interno");
        problem.setDetail("Ocurrió un error inesperado. Por favor intente más tarde.");
        addStandardProperties(problem, request);
        return problem;
    }

    /**
     * Adds standard properties required by the spec: timestamp, traceId, instance
     */
    private void addStandardProperties(ProblemDetail problem, HttpServletRequest request) {
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("traceId", UUID.randomUUID().toString());
    }
}
