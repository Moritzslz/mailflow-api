package de.flowsuite.mailflowapi.common.entity;

import static de.flowsuite.mailflowapi.common.util.Util.BERLIN_ZONE;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private boolean isReplied;
    @NotBlank private String category;
    @NotBlank private String language;

    @Column(name = "from_email_address_encrypted")
    private String fromEmailAddress;

    private String subject;
    @NotNull private ZonedDateTime receivedAt;
    @NotNull private ZonedDateTime processedAt;
    @NotNull private Integer processingTimeInSeconds;
    @NotBlank private String llmUsed;
    @NotNull private Integer inputTokens;
    @NotNull private Integer outputTokens;
    @NotNull private Integer totalTokens;
    @JsonIgnore @NotBlank private String token;
    @JsonIgnore @NotNull private ZonedDateTime tokenExpiresAt;

    @PrePersist
    @PreUpdate
    private void setTimestampsToBerlin() {
        if (receivedAt != null) {
            receivedAt = receivedAt.withZoneSameInstant(BERLIN_ZONE);
        }
        if (processedAt != null) {
            processedAt = processedAt.withZoneSameInstant(BERLIN_ZONE);
        }
        if (tokenExpiresAt != null) {
            tokenExpiresAt = tokenExpiresAt.withZoneSameInstant(BERLIN_ZONE);
        }
    }
}
