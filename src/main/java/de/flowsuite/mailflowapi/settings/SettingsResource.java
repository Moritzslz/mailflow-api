package de.flowsuite.mailflowapi.settings;

import de.flowsuite.mailflowapi.common.entity.Settings;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
class SettingsResource {

    private final SettingsService settingsService;

    public SettingsResource(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @PostMapping("/{customerId}/users/{userId}/settings")
    ResponseEntity<Settings> createSettings(
            @PathVariable long customerId,
            @PathVariable long userId,
            @RequestBody @Valid Settings settings) {
        return ResponseEntity.ok(settingsService.createSettings(customerId, userId, settings));
    }

    @GetMapping("/{customerId}/users/{userId}/settings")
    ResponseEntity<Settings> getSettings(@PathVariable long customerId, @PathVariable long userId) {
        return ResponseEntity.ok(settingsService.getSettings(customerId, userId));
    }

    @PutMapping("/{customerId}/users/{userId}/settings")
    ResponseEntity<Settings> updateSettings(
            @PathVariable long customerId,
            @PathVariable long userId,
            @RequestBody @Valid Settings settings) {
        return ResponseEntity.ok(settingsService.updateSettings(customerId, userId, settings));
    }
}
