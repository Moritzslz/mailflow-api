package de.flowsuite.mailflowapi.client;

import de.flowsuite.mailflowapi.common.entity.Client;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
class ClientResource {

    private final ClientService clientService;

    public ClientResource(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    ResponseEntity<Client> createClient(@RequestBody @Valid Client client) {
        return ResponseEntity.ok(clientService.createClient(client));
    }

    @GetMapping
    ResponseEntity<List<Client>> listClients() {
        return ResponseEntity.ok(clientService.listClients());
    }
}
