package de.flowsuite.mailflowapi.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

@RestControllerAdvice
public class TrimRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(TrimRequestBodyAdvice.class);

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        trimStringFields(body);
        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }

    // TODO only works for classes not records since record fields are final (immutable) therefore field.set causes IllegalAccessException
    private void trimStringFields(Object object) {
        if (object == null) return;

        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(object);
                    LOG.debug("Trimming value: {}", value);
                    if (value != null) {
                        LOG.debug("Trimmed value: {}", value.trim());
                        field.set(object, value.trim());
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
