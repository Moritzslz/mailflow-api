package de.flowsuite.mailflowapi.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

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

    @Id
    @OneToOne(optional = false)
    private Customer customer;

    private boolean isExecutionEnabled;
    private boolean isAutoReplyEnabled;
    private boolean isResponseRatingEnabled;
    @NotBlank private String supportAgentName;

    @Min(168) @Max(744) private int crawlFrequencyInHours;

    private ZonedDateTime lastCrawlAt;
    private ZonedDateTime nextCrawlAt;
    private String emailHtmlTemplate;
    @NotBlank private String mailbox_email_address;
    @NotBlank private String mailbox_password_hash;
    @NotBlank private String imapHost;
    @NotBlank private String smtpHost;
    private int imapPort;
    private int smtpPort;

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
