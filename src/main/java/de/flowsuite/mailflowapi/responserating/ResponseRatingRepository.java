package de.flowsuite.mailflowapi.responserating;

import de.flowsuite.mailflow.common.entity.ResponseRating;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
interface ResponseRatingRepository extends CrudRepository<ResponseRating, Long> {

    List<ResponseRating> findByCustomerId(long userId);

    List<ResponseRating> findByUserId(long userId);

    boolean existsByMessageLogId(long messageLogId);

    @Query(
            """
            SELECT
                COUNT(r) as count,
                COUNT(CASE WHEN r.isSatisfied THEN 1 END) * 1.0 / COUNT(r) AS avgSatisfaction,
                AVG(r.rating) AS avgRating
            FROM ResponseRating r
            WHERE r.customerId = :customerId
              AND r.createdAt BETWEEN :from AND :to
            """)
    List<Object[]> aggregateCountAndAvgSatisfactionAndAvgRatingByCustomer(
            @Param("customerId") long customerId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to);

    @Query(
            """
            SELECT
                COUNT(r) as count,
                COUNT(CASE WHEN r.isSatisfied THEN 1 END) * 1.0 / COUNT(r) AS avgSatisfaction,
                AVG(r.rating) AS avgRating
            FROM ResponseRating r
            WHERE r.userId = :userId
              AND r.createdAt BETWEEN :from AND :to
            """)
    List<Object[]> aggregateCountAndAvgSatisfactionAndAvgRatingByUser(
            @Param("userId") long userId,
            @Param("from") ZonedDateTime from,
            @Param("to") ZonedDateTime to);
}
