package de.flowsuite.mailflowapi.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
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

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        LOG.error(ex.getMessage());

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
        LOG.error("Exception at [{}]:", request.getDescription(false), ex);

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class) != null) {
            httpStatus = AnnotationUtils.getAnnotation(ex.getClass(), ResponseStatus.class).code();
        }

        Map<String, Object> body =
                buildErrorResponseBody(httpStatus, ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(httpStatus).body(body);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDatabaseException(
            DataAccessException ex, WebRequest request) {
        LOG.error(ex.getMessage());

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        String errorMessage = "An unexpected database error occurred.";

        Map<String, Object> body =
                buildErrorResponseBody(httpStatus, errorMessage, request.getDescription(false));
        return ResponseEntity.status(httpStatus).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        LOG.error(ex.getMessage());

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        String errorMessage = "A database constraint has been violated.";

        Map<String, Object> body =
                buildErrorResponseBody(httpStatus, errorMessage, request.getDescription(false));
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
