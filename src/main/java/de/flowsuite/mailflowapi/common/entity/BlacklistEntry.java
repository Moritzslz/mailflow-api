package de.flowsuite.mailflowapi.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
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
    @NotNull private Long userId;

    @JsonIgnore @NotBlank private String blacklistedEmailAddressHash;

    @Column(name = "blacklisted_email_address_encrypted")
    @NotBlank private String blacklistedEmailAddress; // TODO check if valid email
}
