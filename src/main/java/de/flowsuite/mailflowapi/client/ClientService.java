package de.flowsuite.mailflowapi.client;

import de.flowsuite.mailflowapi.common.entity.Client;
import de.flowsuite.mailflowapi.common.util.AesUtil;

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

    Client createClient(Client client) {
        client.setClientSecret(passwordEncoder.encode(client.getPassword()));
        return clientRepository.save(client);
    }

    List<Client> listClients() {
        return (List<Client>) clientRepository.findAll();
    }
}
