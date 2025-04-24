package de.flowsuite.mailflowapi.ragurl;

import de.flowsuite.mailflowapi.common.entity.RagUrl;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ragUrlService.createRagUrl(customerId, ragUrl, jwt));
    }

    @GetMapping("/{customerId}/rag-urls")
    ResponseEntity<List<RagUrl>> listRagUrls(
            @PathVariable long customerId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ragUrlService.listRagUrls(customerId, jwt));
    }

    @DeleteMapping("/{customerId}/rag-urls/{id}")
    ResponseEntity<Void> deleteRagUrl(
            @PathVariable long customerId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        ragUrlService.deleteRagUrl(customerId, id, jwt);
        return ResponseEntity.noContent().build();
    }
}
