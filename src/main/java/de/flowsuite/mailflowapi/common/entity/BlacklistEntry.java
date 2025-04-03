package de.flowsuite.mailflowapi.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blacklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlacklistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    @NotNull private Long customerId;

    @Email @NotBlank private String blacklistedEmailAddress;
}
