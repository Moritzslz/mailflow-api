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
@Table(name = "message_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @NotBlank private String category;
    @NotBlank private String language;
    @Email private String fromEmailAddress;
    private String subject;
    @NotNull private ZonedDateTime receivedAt;
    @NotNull private ZonedDateTime processedAt;
    private int processingTimeInSeconds;
    @NotBlank private String llmUsed;
    private int inputTokens;
    private int outputTokens;
    private int totalTokens;

    @PrePersist
    @PreUpdate
    private void setTimestampsToBerlin() {
        ZoneId berlinZone = ZoneId.of("Europe/Berlin");
        if (receivedAt != null) {
            receivedAt = receivedAt.withZoneSameInstant(berlinZone);
        }
        if (processedAt != null) {
            processedAt = processedAt.withZoneSameInstant(berlinZone);
        }
    }
}
