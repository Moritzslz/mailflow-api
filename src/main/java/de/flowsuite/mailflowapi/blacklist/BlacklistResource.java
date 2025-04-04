package de.flowsuite.mailflowapi.blacklist;

import de.flowsuite.mailflowapi.common.entity.BlacklistEntry;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
class BlacklistResource {

    private final BlacklistService blacklistService;

    BlacklistResource(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @PostMapping("{customerId}/blacklist")
    ResponseEntity<BlacklistEntry> createBlacklistEntry(
            @PathVariable long customerId, @RequestBody @Valid BlacklistEntry blacklistEntry) {
        return ResponseEntity.ok(blacklistService.createBlacklistEntry(blacklistEntry, customerId));
    }

    @GetMapping("{customerId}/blacklist")
    ResponseEntity<List<BlacklistEntry>> getBlacklistEntriesByCustomerId(
            @PathVariable long customerId) {
        return ResponseEntity.ok(
                (List<BlacklistEntry>)
                        blacklistService.getBlacklistEntriesByCustomerId(customerId));
    }

    @DeleteMapping("{customerId}/blacklist/{blacklistId}")
    ResponseEntity<Void> deleteBlacklistEntry(
            @PathVariable long customerId, @PathVariable long blacklistId) {
        BlacklistEntry blacklistEntry =
                blacklistService.getBlacklistEntryByBlacklistId(customerId, blacklistId);
        blacklistService.deleteBlacklistEntry(blacklistEntry);
        return ResponseEntity.noContent().build();
    }
}
