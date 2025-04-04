package de.flowsuite.mailflowapi.user;

import de.flowsuite.mailflowapi.common.entity.User;

import de.flowsuite.mailflowapi.common.util.HmacUtil;
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
        return getByEmailAddress(emailAddress);
    }

    public User getByEmailAddress(String emailAddress) {
        return userRepository
                .findByEmailAddressHash(HmacUtil.hash(emailAddress))
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    public User getById(long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    // TODO for PUT endpoint: customerId cannot be updated
}
