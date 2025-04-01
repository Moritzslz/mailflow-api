package de.flowsuite.mailflowapi.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

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
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

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

    @Override
    public String getUsername() {
        return emailAddress;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority(role));
        authorities.add(new SimpleGrantedAuthority(Authorities.CUSTOMERS_READ.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.CUSTOMERS_WRITE.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.USERS_READ.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.USERS_WRITE.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.SETTINGS_READ.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.SETTINGS_WRITE.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.RAG_URLS_LIST.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.RAG_URLS_WRITE.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.BLACKLIST_LIST.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.BLACKLIST_WRITE.getAuthority()));
        authorities.add(
                new SimpleGrantedAuthority(Authorities.MESSAGE_CATEGORIES_LIST.getAuthority()));
        authorities.add(
                new SimpleGrantedAuthority(Authorities.MESSAGE_CATEGORIES_WRITE.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.MESSAGE_LOG_LIST.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.MESSAGE_LOG_LIST.getAuthority()));

        return authorities;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isAccountLocked;
    }

    @Override
    public boolean isEnabled() {
        return isAccountEnabled;
    }
}
