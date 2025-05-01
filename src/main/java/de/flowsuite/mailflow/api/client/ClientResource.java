package de.flowsuite.mailflow.api.client;

import de.flowsuite.mailflow.common.entity.Client;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clients")
class ClientResource {

    private final ClientService clientService;

    public ClientResource(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    ResponseEntity<Client> createClient(
            @RequestBody @Valid Client client, UriComponentsBuilder uriBuilder) {
        Client createdClient = clientService.createClient(client);

        URI location =
                uriBuilder.path("/clients/{id}").buildAndExpand(createdClient.getId()).toUri();

        return ResponseEntity.created(location).body(createdClient);
    }

    @GetMapping("/{id}")
    ResponseEntity<Client> getClient(@PathVariable long id) {
        return ResponseEntity.ok(clientService.getClient(id));
    }

    @GetMapping
    ResponseEntity<List<Client>> listClients() {
        return ResponseEntity.ok(clientService.listClients());
    }
}
