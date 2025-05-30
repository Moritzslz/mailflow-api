package de.flowsuite.mailflow.api.client;

import de.flowsuite.mailflow.common.entity.Client;
import de.flowsuite.mailflow.common.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflow.common.exception.EntityNotFoundException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService implements UserDetailsService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    ClientService(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String clientName) throws UsernameNotFoundException {
        return findByClientName(clientName);
    }

    public Client findByClientName(String clientName) {
        return clientRepository
                .findByClientName(clientName)
                .orElseThrow(() -> new UsernameNotFoundException("Client not found."));
    }

    Client createClient(ClientResource.CreateClientRequest request) {
        if (clientRepository.existsByClientName(request.clientName())) {
            throw new EntityAlreadyExistsException(Client.class.getSimpleName());
        }

        Client client = new Client();
        client.setClientName(request.clientName());
        client.setClientSecret(passwordEncoder.encode(request.clientSecret()));
        client.setScope(request.scope());

        return clientRepository.save(client);
    }

    Client getClient(long id) {
        return clientRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Client.class.getSimpleName()));
    }

    List<Client> listClients() {
        return (List<Client>) clientRepository.findAll();
    }
}
