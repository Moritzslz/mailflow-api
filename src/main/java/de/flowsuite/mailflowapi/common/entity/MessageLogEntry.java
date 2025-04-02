package de.flowsuite.mailflowapi.common.entity;

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
    private Long id;

    @NotNull private Long userId;
    @NotNull private Long customerID;
    @NotBlank private String category;
    @NotBlank private String language;
    @Email private String fromEmailAddress; // TODO encrypt using AES
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
