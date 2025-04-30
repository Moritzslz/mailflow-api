package de.flowsuite.mailflowapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.flowsuite.mailflow.common.GlobalExceptionHandler;
import de.flowsuite.mailflow.common.exception.InvalidReCaptchaTokenException;
import de.flowsuite.mailflow.common.exception.MissingReCaptchaTokenException;
import de.flowsuite.mailflow.common.exception.ReCaptchaResponseException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

class ReCaptchaFilter extends OncePerRequestFilter {

    private final String reCaptchaHttpHeader;
    private final ReCaptchaService reCaptchaService;
    private final ObjectMapper objectMapper;

    ReCaptchaFilter(String reCaptchaHttpHeader, ReCaptchaService reCaptchaService) {
        this.reCaptchaHttpHeader = reCaptchaHttpHeader;
        this.reCaptchaService = reCaptchaService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String recaptcha = request.getHeader(reCaptchaHttpHeader);
            reCaptchaService.verifyToken(recaptcha);
            filterChain.doFilter(request, response);
        } catch (InvalidReCaptchaTokenException
                | MissingReCaptchaTokenException
                | ReCaptchaResponseException ex) {
            handleExceptionAndRespond(ex, request, response);
        }
    }

    private void handleExceptionAndRespond(
            Exception ex, HttpServletRequest request, HttpServletResponse response) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        if (ex instanceof ReCaptchaResponseException) {
            httpStatus = HttpStatus.BAD_GATEWAY;
        }

        Map<String, Object> body =
                GlobalExceptionHandler.buildErrorResponseBody(
                        httpStatus, ex.getMessage(), request.getRequestURI());

        response.setContentType("application/json");
        response.setStatus(httpStatus.value());

        try {
            response.getWriter().write(objectMapper.writeValueAsString(body));
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }
}
