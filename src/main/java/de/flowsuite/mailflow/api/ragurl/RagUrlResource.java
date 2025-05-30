package de.flowsuite.mailflow.api.ragurl;

import de.flowsuite.mailflow.common.entity.RagUrl;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/customers")
class RagUrlResource {

    private final Logger LOG = LoggerFactory.getLogger(RagUrlResource.class);
    private static final String NOTIFY_RAG_URLS_URI =
            "/notifications/customers/{customerId}/rag-urls/{id}";

    private final RagUrlService ragUrlService;
    private final RestClient ragServiceRestClient;

    RagUrlResource(
            RagUrlService ragUrlService,
            @Qualifier("mailboxServiceRestClient") RestClient ragServiceRestClient) {
        this.ragUrlService = ragUrlService;
        this.ragServiceRestClient = ragServiceRestClient;
    }

    @PostMapping("/{customerId}/rag-urls")
    ResponseEntity<RagUrl> createRagUrl(
            @PathVariable long customerId,
            @RequestBody @Valid RagUrl ragUrl,
            @AuthenticationPrincipal Jwt jwt,
            UriComponentsBuilder uriBuilder) {
        RagUrl createdRagUrl = ragUrlService.createRagUrl(customerId, ragUrl, jwt);

        URI location =
                uriBuilder
                        .path("/customers/{customerId}/rag-urls/{id}")
                        .buildAndExpand(createdRagUrl.getCustomerId(), createdRagUrl.getId())
                        .toUri();

        CompletableFuture.runAsync(() -> notifyRagService(HttpMethod.POST, createdRagUrl, jwt));

        return ResponseEntity.created(location).body(createdRagUrl);
    }

    @GetMapping("/{customerId}/rag-urls/{id}")
    ResponseEntity<RagUrl> getRagUrl(
            @PathVariable long customerId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ragUrlService.getRagUrl(customerId, id, jwt));
    }

    @GetMapping("/{customerId}/rag-urls")
    ResponseEntity<List<RagUrl>> listRagUrls(
            @PathVariable long customerId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ragUrlService.listRagUrls(customerId, jwt));
    }

    @PutMapping("/{customerId}/rag-urls/{id}")
    ResponseEntity<RagUrl> updateRagUrl(
            @PathVariable long customerId,
            @PathVariable long id,
            @RequestBody @Valid RagUrl ragUrl,
            @AuthenticationPrincipal Jwt jwt) {
        RagUrl updatedRagUrl = ragUrlService.updateRagUrl(customerId, id, ragUrl, jwt);
        CompletableFuture.runAsync(() -> notifyRagService(HttpMethod.PUT, updatedRagUrl, jwt));
        return ResponseEntity.ok(ragUrl);
    }

    @PutMapping("/{customerId}/rag-urls/{id}/crawl-status")
    ResponseEntity<RagUrl> updateRagUrlCrawlStatus(
            @PathVariable long customerId,
            @PathVariable long id,
            @RequestBody boolean lastCrawlSuccessful,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                ragUrlService.updateRagUrlCrawlStatus(customerId, id, lastCrawlSuccessful, jwt));
    }

    @DeleteMapping("/{customerId}/rag-urls/{id}")
    ResponseEntity<Void> deleteRagUrl(
            @PathVariable long customerId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        RagUrl deletedRagUrl = ragUrlService.deleteRagUrl(customerId, id, jwt);
        CompletableFuture.runAsync(() -> notifyRagService(HttpMethod.DELETE, deletedRagUrl, jwt));
        return ResponseEntity.noContent().build();
    }

    private void notifyRagService(HttpMethod method, RagUrl ragUrl, Jwt jwt) {
        LOG.debug("Notifying rag service of rag url change");

        ragServiceRestClient
                .method(method)
                .uri(NOTIFY_RAG_URLS_URI, ragUrl.getCustomerId(), ragUrl.getId())
                .body(ragUrl)
                .retrieve()
                .toBodilessEntity();
    }
}
