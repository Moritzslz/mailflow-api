package de.flowsuite.mailflowapi.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.flowsuite.mailflowapi.BaseServiceTest;
import de.flowsuite.mailflowapi.common.entity.Client;
import de.flowsuite.mailflowapi.common.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest extends BaseServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private ClientService clientService;

    private Client testClient;

    private Client buildTestClient() {
        return Client.builder().id(1L).clientName("test-client").clientSecret("secret").build();
    }

    @BeforeEach
    void setup() {
        testClient = buildTestClient();
    }

    @Test
    void testLoadUserByUsername_success() {
        when(clientRepository.findByClientName(testClient.getClientName()))
                .thenReturn(Optional.of(testClient));

        Client client = (Client) clientService.loadUserByUsername(testClient.getClientName());

        assertEquals(testClient, client);
    }

    @Test
    void testLoadUserByUsername_notFound() {
        when(clientRepository.findByClientName(testClient.getClientName()))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> clientService.loadUserByUsername(testClient.getClientName()));
    }

    @Test
    void testFindByClientName_success() {
        when(clientRepository.findByClientName(testClient.getClientName()))
                .thenReturn(Optional.of(testClient));

        Client client = clientService.findByClientName(testClient.getClientName());

        assertEquals(testClient, client);
    }

    @Test
    void testFindByClientName_notFound() {
        when(clientRepository.findByClientName(testClient.getClientName()))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> clientService.findByClientName(testClient.getClientName()));
    }

    @Test
    void testCreateClient_success() {
        when(clientRepository.existsByClientName(testClient.getClientName())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(HASHED_VALUE);

        testClient.setId(null);
        assertNull(testClient.getId());

        clientService.createClient(testClient);

        ArgumentCaptor<Client> clientCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(clientCaptor.capture());
        Client savedClient = clientCaptor.getValue();

        assertEquals(testClient.getClientName(), savedClient.getClientName());
        assertEquals(HASHED_VALUE, savedClient.getClientSecret());
    }

    @Test
    void testCreateClient_idConflict() {
        assertThrows(IdConflictException.class, () -> clientService.createClient(testClient));

        verify(clientRepository, never()).save(any());
    }

    @Test
    void testCreateClient_alreadyExists() {
        when(clientRepository.existsByClientName(testClient.getClientName())).thenReturn(true);

        testClient.setId(null);
        assertNull(testClient.getId());

        assertThrows(
                EntityAlreadyExistsException.class, () -> clientService.createClient(testClient));

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testGetClient_success() {
        when(clientRepository.findById(testClient.getId())).thenReturn(Optional.of(testClient));

        Client client = clientService.getClient(testClient.getId());

        assertEquals(testClient, client);
    }

    @Test
    void testGetClient_notFound() {
        when(clientRepository.findById(testClient.getId())).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class, () -> clientService.getClient(testClient.getId()));
    }

    @Test
    void testListClients_success() {
        when(clientRepository.findAll()).thenReturn(List.of(testClient));

        List<Client> clients = clientService.listClients();

        assertEquals(1, clients.size());
        assertEquals(testClient, clients.get(0));
    }
}
