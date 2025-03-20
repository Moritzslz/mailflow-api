package de.flowsuite.mailflowapi.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

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
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @NotBlank private String category;

    private boolean isReply;
    private boolean isFunctionCall;
    @NotBlank private String description;
}
