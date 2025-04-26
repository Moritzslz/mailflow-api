package de.flowsuite.mailflowapi.common.entity;

import static de.flowsuite.mailflowapi.common.util.Util.BERLIN_ZONE;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.flowsuite.mailflowapi.common.constant.Authorities;

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
    private Long id;

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
    @Builder.Default @JsonIgnore @NotBlank private String role = Authorities.USER.getAuthority();
    @JsonIgnore @NotNull private Boolean isAccountLocked;
    @JsonIgnore @NotNull private Boolean isAccountEnabled;
    @NotNull private Boolean isSubscribedToNewsletter;
    @JsonIgnore @NotBlank private String verificationToken;
    @JsonIgnore @NotNull private ZonedDateTime tokenExpiresAt;
    @JsonIgnore private ZonedDateTime lastLoginAt;
    @JsonIgnore private ZonedDateTime createdAt;
    @JsonIgnore private ZonedDateTime updatedAt;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "user_id")
    private Settings settings;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now(BERLIN_ZONE);
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now(BERLIN_ZONE);
        if (tokenExpiresAt != null) {
            tokenExpiresAt = tokenExpiresAt.withZoneSameInstant(BERLIN_ZONE);
        }
        if (lastLoginAt != null) {
            lastLoginAt = lastLoginAt.withZoneSameInstant(BERLIN_ZONE);
        }
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return emailAddressHash;
    }

    // spotless:off
    @JsonIgnore
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
        authorities.add(new SimpleGrantedAuthority(Authorities.RAG_URLS_READ.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.RAG_URLS_WRITE.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.BLACKLIST_LIST.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.BLACKLIST_READ.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.BLACKLIST_WRITE.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.MESSAGE_CATEGORIES_LIST.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.MESSAGE_CATEGORIES_READ.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.MESSAGE_CATEGORIES_WRITE.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.MESSAGE_LOG_READ.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.MESSAGE_LOG_LIST.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.RESPONSE_RATINGS_LIST.getAuthority()));
        authorities.add(new SimpleGrantedAuthority(Authorities.RESPONSE_RATINGS_READ.getAuthority()));

        return authorities;
    }
    // spotless:on

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return !isAccountLocked;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return isAccountEnabled;
    }
}
