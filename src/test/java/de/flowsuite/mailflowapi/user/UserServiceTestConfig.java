package de.flowsuite.mailflowapi.user;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class UserServiceTestConfig {

    @Bean
    @Primary
    public UserService userService() {
        return mock(UserService.class);
    }
}
