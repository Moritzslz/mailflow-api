package de.flowsuite.mailflowapi.common;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessageTemplate = "Field '%s' %s";
        String errorMessage =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(
                                error ->
                                        String.format(
                                                errorMessageTemplate,
                                                error.getField(),
                                                error.getDefaultMessage()))
                        .collect(Collectors.joining(". "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class) != null) {
            httpStatus = AnnotationUtils.getAnnotation(ex.getClass(), ResponseStatus.class).code();
        }

        return buildErrorResponse(httpStatus, ex.getMessage(), request);
    }

    private ResponseEntity<Object> buildErrorResponse(
            HttpStatus status, String message, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now(ZoneId.of("Europe/Berlin")));
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getDescription(false));

        return ResponseEntity.status(status).body(body);
    }
}
