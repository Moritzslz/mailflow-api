package de.flowsuite.mailflowapi.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.flowsuite.mailflowapi.common.auth.Authorities;

import jakarta.persistence.*;
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

    @Column(updatable = false)
    @NotNull private Long customerId;

    @Column(name = "first_name_encrypted")
    @NotBlank private String firstName;

    @Column(name = "last_name_encrypted")
    @NotBlank private String lastName;

    @JsonIgnore private String emailAddressHash;

    @Column(name = "email_address_encrypted")
    @NotBlank private String emailAddress;

    @Column(name = "password_hash")
    @JsonIgnore
    @NotBlank private String password;

    @Column(name = "phone_number_encrypted")
    private String phoneNumber;

    private String position;
    @JsonIgnore @NotNull private String role = Authorities.USER.getAuthority();
    @JsonIgnore @NotNull private Boolean isAccountLocked;
    @JsonIgnore @NotNull private Boolean isAccountEnabled;
    @NotNull private Boolean isSubscribedToNewsletter;
    @JsonIgnore @NotBlank private String verificationToken;
    @JsonIgnore @NotNull private ZonedDateTime tokenExpiresAt;
    @JsonIgnore private ZonedDateTime lastLoginAt;
    @JsonIgnore private ZonedDateTime createdAt;
    @JsonIgnore private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        ZoneId berlinZone = ZoneId.of("Europe/Berlin");
        createdAt = ZonedDateTime.now(berlinZone);
        updatedAt = createdAt;
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
        return emailAddressHash;
    }

    // spotless:off
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
        authorities.add(new SimpleGrantedAuthority(Authorities.MESSAGE_CATEGORIES_LIST.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.MESSAGE_CATEGORIES_WRITE.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.MESSAGE_LOG_LIST.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.RESPONSE_RATINGS_LIST.getAuthority()));

        return authorities;
    }
    // spotless:on

    @Override
    public boolean isAccountNonLocked() {
        return !isAccountLocked;
    }

    @Override
    public boolean isEnabled() {
        return isAccountEnabled;
    }
}
