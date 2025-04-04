package de.flowsuite.mailflowapi.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank private String clientName;

    @Column(name = "client_secret_hash")
    @NotBlank private String clientSecret;

    @NotBlank private String scope;

    @Override
    public String getUsername() {
        return clientName;
    }

    @Override
    public String getPassword() {
        return clientSecret;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        for (String scope : scope.split("\\s")) {
            authorities.add(new SimpleGrantedAuthority(scope));
        }

        return authorities;
    }
}
