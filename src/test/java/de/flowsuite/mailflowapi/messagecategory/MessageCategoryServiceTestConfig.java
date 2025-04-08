package de.flowsuite.mailflowapi.messagecategory;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MessageCategoryServiceTestConfig {
    @Bean
    @Primary
    public MessageCategoryService messageCategoryService() {
        return mock(MessageCategoryService.class);
    }
}
