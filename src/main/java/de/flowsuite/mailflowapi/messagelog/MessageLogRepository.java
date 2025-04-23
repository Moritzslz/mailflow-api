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
            value =
                    """
    SELECT
        DATE(m.receivedAt),
        m.category AS category,
        COUNT(m) AS count
    FROM MessageLogEntry m
    WHERE m.customerId = :customerId
        AND m.receivedAt BETWEEN :from AND :to
    GROUP BY DATE(m.receivedAt), m.category
    ORDER BY DATE(m.receivedAt) ASC
""")
    List<Object[]> findCategoryCountsGroupedByDayAndCustomerId(
            @Param("customerId") Long customerId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to);

    @Query(
            value =
                    """
    SELECT
        WEEK(m.receivedAt),
        m.category AS category,
        COUNT(m) AS count
    FROM MessageLogEntry m
    WHERE m.customerId = :customerId
        AND m.receivedAt BETWEEN :from AND :to
    GROUP BY WEEK(m.receivedAt), m.category
    ORDER BY WEEK(m.receivedAt) ASC
""")
    List<Object[]> findCategoryCountsGroupedByWeekAndCustomerId(
            @Param("customerId") Long customerId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to);

    @Query(
            value =
                    """
    SELECT
        MONTH(m.receivedAt),
        m.category AS category,
        COUNT(m) AS count
    FROM MessageLogEntry m
    WHERE m.customerId = :customerId
        AND m.receivedAt BETWEEN :from AND :to
    GROUP BY MONTH(m.receivedAt), m.category
    ORDER BY MONTH(m.receivedAt) ASC
""")
    List<Object[]> findCategoryCountsGroupedByMonthAndCustomerId(
            @Param("customerId") Long customerId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to);

    @Query(
            value =
                    """
    SELECT
        YEAR(m.receivedAt),
        m.category AS category,
        COUNT(m) AS count
    FROM MessageLogEntry m
    WHERE m.customerId = :customerId
        AND m.receivedAt BETWEEN :from AND :to
    GROUP BY YEAR(m.receivedAt), m.category
    ORDER BY YEAR(m.receivedAt) ASC
""")
    List<Object[]> findCategoryCountsGroupedByYearAndCustomerId(
            @Param("customerId") Long customerId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to);

    @Query(
            """
    SELECT AVG(m.processingTimeInSeconds)
    FROM MessageLogEntry m
    WHERE m.customerId = :customerId
      AND m.receivedAt BETWEEN :from AND :to
""")
    double findAverageProcessingTimeByCustomerId(
            @Param("customerId") Long customerId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to);

    @Query(
            """
    SELECT
        COUNT(CASE WHEN m.isReplied THEN 1 END) * 1.0 / COUNT(*) AS responseRate
    FROM MessageLogEntry m
    WHERE m.customerId = :customerId
      AND m.receivedAt BETWEEN :from AND :to
""")
    double getResponseRateBetween(
            @Param("customerId") Long customerId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to);
}
