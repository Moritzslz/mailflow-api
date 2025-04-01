package de.flowsuite.mailflowapi.common.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id private Long id;
    @NotNull private String clientId;
    @NotNull private String clientSecret;
    @NotNull private String authenticationMethod;
    @NotNull private String authorizationGrantTypes;
    @NotNull private String scopes;
    @NotNull private String tokenSettings;
}
