package de.flowsuite.mailflowapi.common.entity;

import jakarta.persistence.*;
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
    private Long id;

    @Column(updatable = false)
    @NotNull private Long userId;

    @Column(updatable = false)
    @NotNull private Long customerId;

    @NotBlank private String category;
    @NotBlank private String language;

    @Column(name = "from_email_address_encrypted")
    private String fromEmailAddress; // TODO check if valid email & encrypt

    private String subject;
    @NotNull private ZonedDateTime receivedAt;
    @NotNull private ZonedDateTime processedAt;
    @NotNull private Integer processingTimeInSeconds;
    @NotBlank private String llmUsed;
    @NotNull private Integer inputTokens;
    @NotNull private Integer outputTokens;
    @NotNull private Integer totalTokens;

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
