package de.flowsuite.mailflowapi.settings;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class SettingsServiceTestConfig {
    @Bean
    @Primary
    public SettingsService settingsService() {
        return mock(SettingsService.class);
    }
}
