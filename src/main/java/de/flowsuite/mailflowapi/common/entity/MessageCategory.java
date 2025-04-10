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

    @Column(updatable = false)
    @NotNull private Long userId;

    @NotBlank private String category;

    @Column(name = "is_reply")
    @NotNull private Boolean reply;

    @Column(name = "is_function_call")
    @NotNull private Boolean functionCall;

    @NotBlank private String description;
}
