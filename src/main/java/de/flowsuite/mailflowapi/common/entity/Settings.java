package de.flowsuite.mailflowapi.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
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

    @Column(name = "mailbox_password_encrypted")
    @NotBlank private String mailboxPassword;

    private String imapHost;
    private String smtpHost;
    private Integer imapPort;
    private Integer smtpPort;

    @PrePersist
    @PreUpdate
    private void setTimestampsToBerlin() {
        ZoneId berlinZone = ZoneId.of("Europe/Berlin");
        if (lastCrawlAt != null) {
            lastCrawlAt = lastCrawlAt.withZoneSameInstant(berlinZone);
        }
        if (nextCrawlAt != null) {
            nextCrawlAt = nextCrawlAt.withZoneSameInstant(berlinZone);
        }
    }
}
