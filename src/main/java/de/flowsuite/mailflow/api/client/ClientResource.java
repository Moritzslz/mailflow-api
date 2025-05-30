package de.flowsuite.mailflow.api.client;

import de.flowsuite.mailflow.common.entity.Client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clients")
class ClientResource {

    private final ClientService clientService;

    ClientResource(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    ResponseEntity<Client> createClient(
            @RequestBody @Valid CreateClientRequest request, UriComponentsBuilder uriBuilder) {
        Client createdClient = clientService.createClient(request);

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

    record CreateClientRequest(
            @NotBlank String clientName, @NotBlank String clientSecret, @NotBlank String scope) {}
}
