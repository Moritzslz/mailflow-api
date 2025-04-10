package de.flowsuite.mailflowapi.common.dto;

import jakarta.validation.constraints.NotBlank;

public record Message(@NotBlank String message) {}
