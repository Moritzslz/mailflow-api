package de.flowsuite.mailflowapi.common.entity;

import static de.flowsuite.mailflowapi.common.util.Util.BERLIN_ZONE;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settings {

    @Id @NotNull private Long userId;

    @Column(updatable = false)
    @NotNull private Long customerId;

    private boolean isExecutionEnabled;
    private boolean isAutoReplyEnabled;
    private boolean isResponseRatingEnabled;

    @Min(168) @Max(744) private int crawlFrequencyInHours;

    private ZonedDateTime lastCrawlAt;
    private ZonedDateTime nextCrawlAt;
    @JsonIgnore private String mailboxPasswordHash;

    @Column(name = "mailbox_password_encrypted")
    @NotBlank private String mailboxPassword;

    private String imapHost;
    private String smtpHost;
    private Integer imapPort;
    private Integer smtpPort;

    @PrePersist
    @PreUpdate
    private void setTimestampsToBerlin() {
        if (lastCrawlAt != null) {
            lastCrawlAt = lastCrawlAt.withZoneSameInstant(BERLIN_ZONE);
        }
        if (nextCrawlAt != null) {
            nextCrawlAt = nextCrawlAt.withZoneSameInstant(BERLIN_ZONE);
        }
    }
}
