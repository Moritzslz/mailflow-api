package de.flowsuite.mailflowapi.ragurl;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class RagUrlServiceTestConfig {
    @Bean
    @Primary
    public RagUrlService ragUrlService() {
        return mock(RagUrlService.class);
    }
}
