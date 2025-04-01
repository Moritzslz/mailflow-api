package de.flowsuite.mailflowapi.user;

import de.flowsuite.mailflowapi.common.entity.User;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String emailAddress) throws UsernameNotFoundException {
        return findByEmailAddress(emailAddress);
    }

    public User findByEmailAddress(String emailAddress) {
        return userRepository
                .findByEmailAddress(emailAddress)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    // TODO for PUT endpoint: customerId cannot be updated
}
