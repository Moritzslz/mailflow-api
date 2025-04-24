package de.flowsuite.mailflowapi.messagelog;

import de.flowsuite.mailflowapi.common.entity.MessageLogEntry;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
interface MessageLogRepository extends CrudRepository<MessageLogEntry, Long> {
    List<MessageLogEntry> findByCustomerId(long customerId);

    List<MessageLogEntry> findByCustomerIdAndUserId(long customerId, long userId);

    @Query(
            """
            SELECT
                DATE_TRUNC(:truncUnit, m.receivedAt) AS period,
                m.category AS category,
                COUNT(m) AS count
            FROM MessageLogEntry m
            WHERE m.customerId = :customerId
                AND m.receivedAt BETWEEN :from AND :to
            GROUP BY period, m.category
            """)
    List<Object[]> aggregateCategoryCountsByCustomer(
            @Param("truncUnit") String truncUnit,
            @Param("customerId") long customerId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to);

    @Query(
            """
            SELECT
                AVG(m.processingTimeInSeconds) AS avgProcessingTime,
                COUNT(CASE WHEN m.isReplied THEN 1 END) * 1.0 / COUNT(m) AS responseRate
            FROM MessageLogEntry m
            WHERE m.customerId = :customerId
              AND m.receivedAt BETWEEN :from AND :to
            """)
    List<Object[]> aggregateAvgProcessingTimeAndResponseRateByCustomer(
            @Param("customerId") long customerId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to);

    @Query(
            """
            SELECT
                DATE_TRUNC(:truncUnit, m.receivedAt) AS period,
                m.category AS category,
                COUNT(m) AS count
            FROM MessageLogEntry m
            WHERE m.userId = :userId
                AND m.receivedAt BETWEEN :from AND :to
            GROUP BY period, m.category
            """)
    List<Object[]> aggregateCategoryCountsByUser(
            @Param("truncUnit") String truncUnit,
            @Param("customerId") long userId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to);

    @Query(
            """
            SELECT
                AVG(m.processingTimeInSeconds) AS avgProcessingTime,
                COUNT(CASE WHEN m.isReplied THEN 1 END) * 1.0 / COUNT(m) AS responseRate
            FROM MessageLogEntry m
            WHERE m.customerId = :userId
              AND m.receivedAt BETWEEN :from AND :to
            """)
    List<Object[]> aggregateAvgProcessingTimeAndResponseRateByUser(
            @Param("customerId") long userId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to);
}
