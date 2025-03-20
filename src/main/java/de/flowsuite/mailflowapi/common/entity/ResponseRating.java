package de.flowsuite.mailflowapi.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "response_ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseRating {

    @Id
    @OneToOne(optional = false)
    @Column(name = "id")
    private MessageLogEntry messageLogEntry;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Min(0) @Max(5) private int rating;

    private String feedback;

    @NotNull private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
    }
}
