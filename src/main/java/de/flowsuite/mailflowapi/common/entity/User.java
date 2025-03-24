package de.flowsuite.mailflowapi.common.entity;

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
    @NotNull private Long id;

    @NotNull private Long customerId;

    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @Email @NotBlank private String emailAddress;

    @Column(name = "password_hash")
    @NotBlank private String password;

    private String phoneNumber;

    @NotNull private String role;

    @NotNull private Boolean isAccountLocked;
    @NotNull private Boolean isAccountEnabled;
    @NotNull private Boolean isSubscribedToNewsletter;
    @NotBlank private String verificationToken;
    @NotNull private ZonedDateTime tokenExpiresAt;
    private ZonedDateTime lastLoginAt;
    @NotNull private ZonedDateTime createdAt;
    @NotNull private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        ZoneId berlinZone = ZoneId.of("Europe/Berlin");
        createdAt = ZonedDateTime.now(berlinZone);
        updatedAt = createdAt;
        tokenExpiresAt = createdAt.plusMinutes(30);
    }

    @PreUpdate
    protected void onUpdate() {
        ZoneId berlinZone = ZoneId.of("Europe/Berlin");
        updatedAt = ZonedDateTime.now(berlinZone);
        if (tokenExpiresAt != null) {
            tokenExpiresAt = tokenExpiresAt.withZoneSameInstant(berlinZone);
        }
        if (lastLoginAt != null) {
            lastLoginAt = lastLoginAt.withZoneSameInstant(berlinZone);
        }
    }
}
