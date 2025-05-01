package de.flowsuite.mailflow.api.client;

import de.flowsuite.mailflow.common.entity.Client;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface ClientRepository extends CrudRepository<Client, Long> {

    Optional<Client> findByClientName(String clientName);

    boolean existsByClientName(String clientName);
}
