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
@Table(name = "customer_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSettings {

    @Id @NotNull private Long customerId;
    @NotNull private Boolean isExecutionEnabled;
    @NotNull private Boolean isAutoReplyEnabled;
    @NotNull private Boolean isResponseRatingEnabled;
    @NotBlank private String supportAgentName;

    @Min(168) @Max(744) private int crawlFrequencyInHours;

    private ZonedDateTime lastCrawlAt;
    private ZonedDateTime nextCrawlAt;
    @Email @NotBlank private String mailboxEmailAddress;

    @Column(name = "mailbox_password_hash")
    @NotBlank private String mailboxPassword;

    @NotBlank private String imapHost;
    @NotBlank private String smtpHost;
    @NotNull private Integer imapPort;
    @NotNull private Integer smtpPort;

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
