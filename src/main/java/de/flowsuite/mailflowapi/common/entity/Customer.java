package de.flowsuite.mailflowapi.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank private String company;
    @NotBlank private String street;
    @NotBlank private String houseNumber;
    @NotBlank private String postalCode;
    @NotBlank private String city;

    @Column(updatable = false)
    @NotBlank private String openaiApiKey;

    private String sourceOfContact;
    private String websiteUrl;
    private String privacyPolicyUrl;
    private String ctaUrl;
}
