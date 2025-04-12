package de.flowsuite.mailflowapi.messagelog;

import de.flowsuite.mailflowapi.common.entity.MessageLogEntry;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface MessageLogRepository extends CrudRepository<MessageLogEntry, Long> {
    Iterable<MessageLogEntry> findByCustomerId(long customerId);
    Iterable<MessageLogEntry> findByCustomerIdAndCategoryAndReceivedAtBetween(Long, customerId, String category, ZonedDateTime from, ZonedDateTime to);
    Iterable<MessageLogEntry> findByCustomerIdAndCategory(Long, customerId, String category);
    Iterable<MessageLogEntry> findByCustomerIdAndReceivedAtBetween(Long customerId, ZonedDateTime from, ZonedDateTime to);
    
    @Query("SELECT AVG(m.processingTimeInSeconds), COUNT(m) FROM MessageLogEntry m WHERE m.customerId = :customerId AND m.category = :category")
     Object[] getAverageProcessingTimeAndCountByCategory(@Param("customerId) Long customerId, @Param("category") String category);

    @Query("SELECT AVG(m.processingTimeInSeconds), COUNT(m) FROM MessageLogEntry m WHERE m.customerId = :customerId AND m.receivedAt BETWEEN :from AND :to")
    Object[] getAverageProcessingTimeAndCountBetween(@Param("customerId) Long customerId, @Param("from") ZonedDateTime from, @Param("to")  ZonedDateTime to);

   @Query("SELECT AVG(m.processingTimeInSeconds), COUNT(m) FROM MessageLogEntry m WHERE m.customerId = :customerId AND m.category = :category  AND m.receivedAt BETWEEN :from AND :to")
Object[] getAverageProcessingTimeAndCountByCategoryBetween(@Param("customerId) Long customerId, @Param("category") String category, 
                                                           @Param("from") ZonedDateTime from, 
                                                           @Param("to") ZonedDateTime to);

@Query("SELECT COUNT(m) / COUNT(DISTINCT FUNCTION('DATE', m.receivedAt)) " +
       "FROM MessageLogEntry m " +
       "WHERE m.customerId = :customerId AND m.receivedAt BETWEEN :from AND :to")
Double getAverageMessagesPerDay(@Param("customerId) Long customerId, @Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

@Query("SELECT COUNT(m) / COUNT(DISTINCT FUNCTION('YEARWEEK', m.receivedAt)) " +
       "FROM MessageLogEntry m " +
       "WHERE m.customerId = :customerId AND m.receivedAt BETWEEN :from AND :to")
Double getAverageMessagesPerWeek(@Param("customerId) Long customerId, @Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

@Query("SELECT COUNT(m) / COUNT(DISTINCT FUNCTION('YEAR_MONTH', m.receivedAt)) " +
       "FROM MessageLogEntry m " +
       "WHERE m.customerId = :customerId AND m.receivedAt BETWEEN :from AND :to")
Double getAverageMessagesPerMonth(@Param("customerId) Long customerId, @Param("from") ZonedDateTime from, @Param("to") ZonedDateTime to);

                                           
                                           
}
