package de.flowsuite.mailflowapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @Email @NotBlank private String emailAddress;
    @NotBlank private String passwordHash;
    private String phoneNumber;

    @NotNull private String role;

    private boolean isAccountLocked;
    private boolean isAccountEnabled;
    private boolean isSubscribedToNewsletter;
    @NotBlank private String verificationToken;
    @NotNull private ZonedDateTime tokenExpiresAt;
    @NotNull private ZonedDateTime lastLoginAt;
    @NotNull private ZonedDateTime createdAt;
    @NotNull private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
    }
}
