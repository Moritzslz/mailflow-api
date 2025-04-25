package de.flowsuite.mailflowapi.customer;

import de.flowsuite.mailflowapi.common.entity.Customer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface CustomerRepository extends CrudRepository<Customer, Long> {

    boolean existsByBillingEmailAddress(String billingEmailAddress);

    boolean existsByRegistrationToken(String registrationToken);

    Optional<Customer> findByRegistrationToken(String registrationToken);
}
