package de.flowsuite.mailflow.api.ragurl;

import de.flowsuite.mailflow.common.entity.RagUrl;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/customers")
class RagUrlResource {

    private final RagUrlService ragUrlService;

    RagUrlResource(RagUrlService ragUrlService) {
        this.ragUrlService = ragUrlService;
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

        // Todo notify rag service

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
        // Todo notify rag service
        return ResponseEntity.ok(ragUrlService.updateRagUrl(customerId, id, ragUrl, jwt));
    }

    @PutMapping("/{customerId}/rag-urls/{id}/crawl-status")
    ResponseEntity<RagUrl> updateRagUrlCrawlStatus(
            @PathVariable long customerId,
            @PathVariable long id,
            @RequestBody boolean lastCrawlSuccessful,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ragUrlService.updateRagUrlCrawlStatus(customerId, id, lastCrawlSuccessful, jwt));
    }

    @DeleteMapping("/{customerId}/rag-urls/{id}")
    ResponseEntity<Void> deleteRagUrl(
            @PathVariable long customerId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        ragUrlService.deleteRagUrl(customerId, id, jwt);
        // Todo notify rag service
        return ResponseEntity.noContent().build();
    }
}
