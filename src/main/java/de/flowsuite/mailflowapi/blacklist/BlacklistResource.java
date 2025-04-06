package de.flowsuite.mailflowapi.blacklist;

import de.flowsuite.mailflowapi.common.entity.BlacklistEntry;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
class BlacklistResource {

    private final BlacklistService blacklistService;

    BlacklistResource(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @PostMapping("/{customerId}/users/{userId}/blacklist")
    ResponseEntity<BlacklistEntry> createBlacklistEntry(
            @PathVariable long customerId,
            @PathVariable long userId,
            @RequestBody @Valid BlacklistEntry blacklistEntry,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                blacklistService.createBlacklistEntry(customerId, userId, blacklistEntry, jwt));
    }

    @GetMapping("/{customerId}/users/{userId}/blacklist")
    ResponseEntity<List<BlacklistEntry>> listBlacklistEntries(
            @PathVariable long customerId,
            @PathVariable long userId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(blacklistService.listBlacklistEntries(customerId, userId, jwt));
    }

    @DeleteMapping("/{customerId}/users/{userId}/blacklist/{blacklistEntryId}")
    ResponseEntity<Void> deleteBlacklistEntry(
            @PathVariable long customerId,
            @PathVariable long userId,
            @PathVariable long blacklistEntryId,
            @AuthenticationPrincipal Jwt jwt) {
        blacklistService.deleteBlacklistEntry(customerId, userId, blacklistEntryId, jwt);
        return ResponseEntity.noContent().build();
    }
}
