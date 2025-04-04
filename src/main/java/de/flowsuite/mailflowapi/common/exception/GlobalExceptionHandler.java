package de.flowsuite.mailflowapi.common.exception;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
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

        Map<String, Object> body =
                buildErrorResponseBody(httpStatus, errorMessage, request.getDescription(false));
        return ResponseEntity.status(httpStatus).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class) != null) {
            httpStatus = AnnotationUtils.getAnnotation(ex.getClass(), ResponseStatus.class).code();
        }

        Map<String, Object> body =
                buildErrorResponseBody(httpStatus, ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(httpStatus).body(body);
    }

    public static Map<String, Object> buildErrorResponseBody(
            HttpStatus status, String message, String path) {

        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);

        return body;
    }
}
