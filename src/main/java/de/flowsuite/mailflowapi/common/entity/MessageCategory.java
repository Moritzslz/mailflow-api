package de.flowsuite.mailflowapi.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "message_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private Long customerId;
    @NotBlank private String category;
    @NotNull private Boolean isReply;
    @NotNull private Boolean isFunctionCall;
    @NotBlank private String description;
}
