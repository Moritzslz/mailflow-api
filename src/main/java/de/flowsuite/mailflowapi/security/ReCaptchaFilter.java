package de.flowsuite.mailflowapi.security;

import de.flowsuite.mailflowapi.security.recaptcha.ReCaptchaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
// TODO
class ReCaptchaFilter extends OncePerRequestFilter {

    private final ReCaptchaService reCaptchaService;

    ReCaptchaFilter(ReCaptchaService reCaptchaService) {
        this.reCaptchaService = reCaptchaService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getMethod().equals("POST")) {
            String recaptcha = request.getHeader(SecurityConfig.RECAPTCHA_HEADER);
            reCaptchaService.verifyToken(recaptcha);
        }
        filterChain.doFilter(request, response);
    }
}
