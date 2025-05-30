package de.flowsuite.mailflow.api.user;

import de.flowsuite.mailflow.common.entity.User;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmailAddressHash(String emailAddressHash);

    Optional<User> findByVerificationToken(String verificationToken);

    Set<User> findAllBySubscribedToNewsletter(Boolean isSubscribedToNewsletter);

    boolean existsByEmailAddressHash(String emailAddressHash);

    boolean existsByVerificationToken(String verificationToken);

    List<User> findAllByCustomerId(long customerId);
}
