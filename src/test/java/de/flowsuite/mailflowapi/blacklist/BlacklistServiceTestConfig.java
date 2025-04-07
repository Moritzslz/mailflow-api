package de.flowsuite.mailflowapi.blacklist;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class BlacklistServiceTestConfig {

    @Bean
    @Primary
    public BlacklistService blacklistService() {
        return mock(BlacklistService.class);
    }
}
