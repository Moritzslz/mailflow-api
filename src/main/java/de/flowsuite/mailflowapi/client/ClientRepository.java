package de.flowsuite.mailflowapi.client;

import de.flowsuite.mailflowapi.common.entity.Client;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends CrudRepository<Client, Long> {
    Optional<Client> findByClientId(String clientId);
}
